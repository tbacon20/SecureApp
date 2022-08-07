import json
import boto3
import botocore
import hashlib
import base64
import time
from boto3.dynamodb.conditions import Attr


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
    
def lambda_handler(event, context):
   
    # load needed user data
    # user & session id are both strings
    clear_session_id = event['clear_session_id']
    username = event['username']
    #clear_session_id="ef8uUhuwLIPb9RCeVQKyjvAYmsWuEA3vvzgVWEkqQhc="
    # need to modify this later on
    client_id = "5l3d7sqrola360npgonvlptjb"
    #Set New Status
    status = 'connected'
    #now we need to verify that the user's session id is valid & open
    
    kwargs = {
        'FilterExpression': Attr("session_status").eq("open"),
        'FilterExpression' : Attr("session_id").eq(clear_session_id)
    }
    
    
    final_records = scan_db("sessions_are_us", kwargs)
    #checks if session is open and does exist
    if len(final_records) == 0:
        return {
            'status' : 'dynamodb error'
        }
        
    dynamodb = boto3.resource('dynamodb', region_name='us-west-2')

    table = dynamodb.Table('sessions_are_us')
    expiration = int(time.time()) + 28800
    
    response = table.update_item(
        Key={
            'session_id': clear_session_id
        },
        UpdateExpression="set #session_connection=:r",
        ExpressionAttributeNames={
            '#session_connection': 'session_status'
        },
        ExpressionAttributeValues={
            ':r': "Connected"
           
        },
        ReturnValues="UPDATED_NEW"
    )

    response = table.update_item(
        Key={
            'session_id': clear_session_id
        },
        UpdateExpression="set #session_ttl=:r",
        ExpressionAttributeNames={
            '#session_ttl': 'ttl'
        },
        ExpressionAttributeValues={
            ':r': expiration
           
        },
        ReturnValues="UPDATED_NEW"
    )    
    
    
    return {
        'status' : 200
    }
