import json
import boto3

def lambda_handler(event, context):
    
    dynamodb = boto3.resource('dynamodb')
    
    sessions_are_us_table = dynamodb.Table('keys_are_us')

    try: 
        session_response = sessions_are_us_table.put_item(
        Item={
            'user_id' : event['user_id'],
            'ecc_key_id' : event['ecc_key_id'],
            'ecc_public_key' : event['ecc_public_key'],
            'ttl': event['ttl']
        }    
    )
    except:
        return {'error' : "DynamoDb input error"}    
    
    
    
    return {
        'statusCode': 200
    }
