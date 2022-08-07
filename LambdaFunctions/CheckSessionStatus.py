import json
import boto3
    
def lambda_handler(event, context):
    
    # TESTING VARIABLES
    #clear_session_id = "LxKFraw73CaSkSWEcgltD9CxeAH6xg5Q642s9mFz8OI="
    clear_session_id = event['session_id_unsigned']
    
    client = boto3.client('dynamodb')

    response = client.get_item(
        TableName='sessions_are_us',
        Key={
        'session_id': {
            'S': clear_session_id,
            }
        },
        AttributesToGet=[
        'jwt_token',
        'session_status'
    ]
    )    
    
    if response['Item']['session_status']['S'] == "Authenticated":
        if 'jwt_token' in response['Item']:
            return {
                'jwt_token' : response['Item']['jwt_token']['S']
            }
        else: 
            return {
                'jwt_token' : 511
            }
    else:
        return{
            'jwt_token': 401
        }