import json
import boto3
import botocore
import hashlib
import cryptography
import base64
import time
import uuid
from boto3.dynamodb.conditions import Attr
from cryptography.hazmat.primitives.asymmetric import ec
from cryptography.hazmat.primitives import hashes
from cryptography.hazmat.primitives import serialization
from base64 import b64decode, b64encode

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
    
    dynamodb = boto3.resource('dynamodb')
    
    sessions_are_us_table = dynamodb.Table('sessions_are_us')
    
    status = 'open'
    
    expiration = int(time.time()) + 180
    creation = int(time.time())
    
    #Getting lambda function ID
    session_id = str.encode(str(uuid.uuid1()))
    #Hash it
    hashed_session_id = hashlib.sha256(session_id).digest()
    formatted_hashed = b64encode(hashed_session_id).decode('utf-8')
    try: 
        session_response = sessions_are_us_table.put_item(
        Item={
            'session_id' : formatted_hashed,
            'session_status' : status,
            'session_datetime_creation' : creation,
            'ttl': expiration
        }    
    )
    except:
        return {'error' : "DynamoDb input error"}

    #Done with generation of session id -> now for signing
    # Load private key
    new_data = get_secret()
    ecc_private_key = new_data['ecc_private_key_un']
    #PLEASE DO NOT TOUCH PLEAASE
    correct_formatted = b64decode(ecc_private_key.encode('utf-8'))
    #Loading of ecc private key complete here
    loaded_private_key = serialization.load_der_private_key(
        correct_formatted,
        password=None
        )
        
    signed_session_id = loaded_private_key.sign(hashed_session_id, ec.ECDSA(hashes.SHA256()))
        
    formatted_signed = b64encode(signed_session_id).decode('utf-8')
   
    #print(formatted_signed)
    return {
            'signed_session_id' : formatted_signed,
            'clear_session_id' : formatted_hashed
            
    }
