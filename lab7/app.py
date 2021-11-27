import base64
import pymongo
from Crypto.PublicKey import RSA
from Crypto.Cipher import PKCS1_OAEP


# Create the client
client = pymongo.MongoClient('localhost', 27017)

# Connect to database
db = client['lab7']

# Fetch users collection
users_collection = db['users']

def generate_keys():
    """Function generates private and public keys."""
    try:
        new_key = RSA.generate(1024)
        private_key = new_key.exportKey("PEM")
        public_key = new_key.publickey().exportKey("PEM")

        with open("private_key.pem", "wb") as key_file:
            key_file.write(private_key)

        with open("public_key.pem", "wb") as key_file:
            key_file.write(public_key)
    except:
        raise NotImplementedError
    else:
        return "Keys were generated successfully!"

def encrypt_item(item, public_key):
    """Function for encrypting some item."""
    rsa_key = RSA.importKey(public_key)
    encryptor = PKCS1_OAEP.new(rsa_key)
    encrypted = encryptor.encrypt(item.encode())

    return base64.b64encode(encrypted)

def decrypt_item(item, private_key):
    """Function for decrypting encrypted value."""
    rsa_key = RSA.importKey(private_key)
    decryptor = PKCS1_OAEP.new(rsa_key)

    item = base64.b64decode(item, b'+/')

    decrypted = decryptor.decrypt(item)

    return  decrypted.decode()

def insert_users(collection):
    """Inserting info in MongoDB database"""
    new_users = [
        {"id": 1,
         "first_name": "John",
         "last_name": "White",
         "gender": "Male",
         "email": "john.white@example.com",
         "ip_address": "71.58.82.108"},

        {"id": 2,
         "first_name": "Camila",
         "last_name": "Black",
         "gender": "Female",
         "email": "camila.black@example.com",
         "ip_address": "173.164.73.210"},

        {"id": 3,
         "first_name": "Luisa",
         "last_name": "Green",
         "gender": "Female",
         "email": "luisa.green@example.com",
         "ip_address": "75.157.245.166"},

        {"id": 4,
         "first_name": "Jake",
         "last_name": "Smith",
         "gender": "Male",
         "email": "jake.smith@example.com",
         "ip_address": "170.100.152.189"},

        {"id": 5,
         "first_name": "Peter",
         "last_name": "Parker",
         "gender": "Male",
         "email": "peter.parker@example.com",
         "ip_address": "36.212.199.196"},

        {"id": 6,
         "first_name": "Tom",
         "last_name": "Reynolds",
         "gender": "Male",
         "email": "tom.reynolds@example.com",
         "ip_address": "158.185.127.22"},

        {"id": 7,
         "first_name": "Jane",
         "last_name": "Grey",
         "gender": "Female",
         "email": "jane.grey@example.com",
         "ip_address": "184.146.161.24"},

        {"id": 8,
         "first_name": "Jake",
         "last_name": "Brown",
         "gender": "Male",
         "email": "jake.brown@example.com",
         "ip_address": "186.19.71.71"},

        {"id": 9,
         "first_name": "Lili",
         "last_name": "Vans",
         "gender": "Female",
         "email": "lili.vans@example.com",
         "ip_address": "177.195.173.196"},

        {"id": 10,
         "first_name": "Cole",
         "last_name": "Doyle",
         "gender": "Male",
         "email": "cole.doyle@example.com",
         "ip_address": "26.227.221.169"}
    ]

    try:
        with open("public_key.pem", "rb") as key_file:
            public_key = key_file.read()

        for user in new_users:
            user['email'] = encrypt_item(user['email'], public_key)
            user['ip_address'] = encrypt_item(user['ip_address'], public_key)

        collection.insert_many(new_users)
    except:
        raise NotImplementedError
    else:
        return 'Info inserted successfully!'

def show_users(collection):
    """Show contents of db collection."""
    users = collection.find()
    for i in range(users.count()):
        for user in users:
            print(user)


if __name__ == "__main__":
    generate_keys()
    print(insert_users(users_collection))

    with open("private_key.pem", "rb") as key_file:
        private_key = key_file.read()

    users = users_collection.find()
    for user in users:
        user['email'] = decrypt_item(user['email'], private_key)
        users_collection.update_many({}, {'$set': {'email': user['email']}})
        user['ip_address'] = decrypt_item(user['ip_address'], private_key)
        users_collection.update_many({}, {'$set': {'ip_address': user['ip_address']}})

    show_users(users_collection)