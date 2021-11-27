# Lab 6: SSO Security

>The main goal of this lab work is to develop an application to 
analyze the sensitive data that is sent to applications when using SSO.


### Features

- Offer user authentication via SSO using at least 3 identity providers (e.g. Facebook,
Gmail, Twitter etc.);
- Configure SSO integration to get as much as possible data about the end-user;
- Output all data which was provided by the identity providers (e.g. user’s name, age,
gender, email etc.).

### Used Technologies:

- Python
- Flask
- Ngrok


### Instructions:
**1. Install the following libraries:**
- flask
- requests_oauthlib
- python-dotenv

**2. Create an .env file which contains the following information:**

```
CLIENT_ID = "your simple login app id"
CLIENT_SECRET = "your simple login app secret"

FB_CLIENT_ID = "your facebook app id"
FB_CLIENT_SECRET = "your facebook app secret"

G_CLIENT_ID = "your github app id"
G_CLIENT_SECRET = "your github app secret"

FB_URL = "your facebook valid oauth redirect URI"
```

**3. Run server:**
```
python sso.py
```
