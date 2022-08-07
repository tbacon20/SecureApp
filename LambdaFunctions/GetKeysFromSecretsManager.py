import json
import boto3

def get_secret():
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
   
    '''
    keys_response
    dict_keys(['ARN', 'Name', 'VersionId', 'SecretString', 'VersionStages', 'CreatedDate', 'ResponseMetadata'])
    
    dictionary_keys
    dict_keys(['date_time', 'date_time_expiring', 'rsa_key_id', 'rsa_private_key', 'rsa_private_key_un', 'rsa_public_key', 'ecc_key_id', 'ecc_private_key', 'ecc_private_key_un', 'ecc_public_key'])
    '''
    
    dictionary_keys = json.loads(keys_response['SecretString'])
    return dictionary_keys
    
def lambda_handler(event, context):
    new_data = get_secret() 

    return {
        'system_date_time_expiring' : new_data['system_date_time_expiring'],
        'rsa_key_id' : new_data['rsa_key_id'],
        'rsa_public_key' : new_data['rsa_public_key'],
        'ecc_key_id' : new_data['ecc_key_id'],
        'ecc_public_key' : new_data['ecc_public_key'] 
        
    }
