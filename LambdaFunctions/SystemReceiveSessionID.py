import json
import boto3
import botocore
import hashlib
import cryptography
import base64

from boto3.dynamodb.conditions import Attr
from cryptography.hazmat.primitives.asymmetric import ec
from cryptography.hazmat.primitives import hashes
from cryptography.hazmat.primitives import serialization
from base64 import b64decode, b64encode

'''
So we are expecting a user's message:
1. (hashed? with sha-256?) 
2. signed with their own ECC private key
3. then add the their own user id
4. then encrypt the message with the RSA public key of the system

What do we need to do in this function?
1. Get the user's message from api, done through appsync -> event dictionary
2. Item will be encrypted
3. Once first layer is decrypted, get user id and see if it exists.
4. Have error handling for user id not existing.
5. If user exists, and is valid, get user's public ECC key.
6. Decrypt further to see session id
7. Verify session id exists
8. If exists, and everything matches, handshake completed.
9. Issue OK, return OK & JWT token
10. User will initiate subscription/websocket connection to specific user
'''



def get_token(username):
    
    client = boto3.client('cognito-idp')
    response = client.admin_initiate_auth(
        UserPoolId='us-west-2_o6NbVz7yP',
        ClientId='77d27s277b4cb5cljh0m9444cf',
        AuthFlow='CUSTOM_AUTH',
        AuthParameters={
            'USERNAME' : username
            }
    )
    return(response['AuthenticationResult']['IdToken'])

def scan_db(table, scan_kwargs=None):
    """
    Get all records of the dynamodb table where the FilterExpression holds true
    :param scan_kwargs: Used to pass filter conditions
    :type scan_kwargs: dict
    :param table: dynamodb table name
    :type table: str
    :return: list of records
    :rtype: dict
    """
    if scan_kwargs is None:
        scan_kwargs = {}
    dynamodb = boto3.resource('dynamodb')
    table = dynamodb.Table(table)

    complete = False
    records = []
    while not complete:
        try:
            response = table.scan(**scan_kwargs)
        except botocore.exceptions.ClientError as error:
            raise Exception('Error quering DB: {}'.format(error))

        records.extend(response.get('Items', []))
        next_key = response.get('LastEvaluatedKey')
        scan_kwargs['ExclusiveStartKey'] = next_key

        complete = True if next_key is None else False
    return records

def get_secret():
    '''
    void input
    access and return secret from secretsmanager
    '''
    secret_name = "system_keys"
    region_name = "us-west-2"
    # Create a Secrets Manager client
    session = boto3.session.Session()
    client = session.client(
        service_name='secretsmanager',
        region_name=region_name
    )
    
    keys_response = client.get_secret_value(
        SecretId='arn:aws:secretsmanager:us-west-2:006840835651:secret:system_keys-kRJjS3',
    )
    
    dictionary_keys = json.loads(keys_response['SecretString'])
    return dictionary_keys

def lambda_handler(event, context):
    
    #load all items from event currently in string format 
    secret_string = event['secret_string']
    #convert to bytes
    secret_string_bytes = str.encode(secret_string)
    username = event['username']
    
    kwargs = {
            'FilterExpression' : Attr("user_id").eq(username)
            }
    
    #Scan database for user
    records = scan_db("keys_are_us", kwargs)
     
    # so if null, user either doesn't exist or hasn't sent keys yet to the system
    if len(records) == 0:
        return {
        'statusCode' : 401
        }
    
    #Get key from row
    user_public_key_ECC = records['ecc_public_key']
    
    # Formatting? it will be string -> needs bytes 
    user_public_key_ECC = str.encode(user_public_key_ECC)

    #Load user's ecc public key 
    loaded_user_ecc_public_key = serialization.load_der_public_key(
            user_public_key_ECC
            password=None
            )

    #Get RSA private key
    secret = get_secret()
    rsa_private_key = secret['rsa_private_key_un']
    correct_formatted = b64decode(rsa_private_key.encode('utf-8'))

    # We now need the rsa private key to be loaded
    #Load actual key for use
    loaded_private_key = serialization.load_der_private_key(
            correct_formatted,
            password=None
            )

    # Now for first layer of decrypting keeps everything in bytes
    first_layer_unecrypted = loaded_private_key.decrypt(secret_string_bytes)

    # Now for the second layer
    session_id_bytes = loaded_user_ecc_public_key.decrypt(first_layer_unecrypted)
    session_id = session_id_bytes.decode() 
    # ok so we should have the session id, now we see if it exists!
    kwargs.clear()
    kwargs = {
            'FilterExpression' : session_id
            }
    # See if the session exists if not return 401, tell user to restart webpage
    session_records = scan_db("sessions_are_us", kwargs)
    if len(session_records) == 0:
        return {
        'statusCode' : 401
        }
    
    # We have now established that yes, the user is who they are, and the system has identified their session 
    # we now have to set the jwt token

    jwt_token = get_token(username)
    dynamodb = boto3.resource('dynamodb', region_name='us-west-2')

    table = dynamodb.Table('sessions_are_us')
    

    response = table.update_item(
        Key={
            'session_id': clear_session_id
        },
        UpdateExpression="set #session_jwt_token=:r",
        ExpressionAttributeNames={
            '#session_jwt_token': 'jwt_token'
        },
        ExpressionAttributeValues={
            ':r': jwt_token
           
        },
        ReturnValues="UPDATED_NEW"
    )  

    return {

            'statusCode' : 200

        }