# Lab 8: Email Confirmation

### Objectives

- Create an application that could register a new user;
- Perform email confirmation (via a one time password / code or via a link);
- Output on the screen whether a user confirmed their email or did not confirm it yet.

### Used Technologies:

- Java
- Java Swing
- Java Mail

### Instructions:
**1. Create a file named login.properties with the following structure:**
*(it keeps the user and password for the sender's gmail):*

```
user=your_username
password=your_password
```
**3. Write the path to this file in app.java:**
```
58. FileReader reader=new FileReader("properties_path_here");
```
**2. Run app.java**

