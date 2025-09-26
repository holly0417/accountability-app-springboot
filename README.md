# Accountability App

Accountability App is a web-based application designed to help you complete day-to-day tasks using a rewards-based accountability system. 
The purpose of this app is to bring a form of instant gratification to our mundane, every-day tasks but in a way that feels more meaningful via actual accountability from other people. 
Users can request partnerships with other users, keep track of payments, and earn "money" through having their completed tasks approved by their partners. 
You can compare your earnings with that of your partners and "spend" those earnings through registering payments into your wallet. <br>

Disclaimer: This app is still very much in development! It's my personal sandbox project for learning how to develop apps. 
So if you'd like to pull this image, know that it may not run as smoothly as you may expect. 

## Built With
* Spring Web framework
* [Spring Boot 3.5+](https://spring.io/projects/spring-boot)
* [Flyway](org.flywaydb:flyway-core) - Database Migration
* [Gradle 9.0](https://gradle.org/) - Dependency Management
* [H2](https://www.h2database.com/html/main.html) - Database
* [Hibernate](https://hibernate.org/) - ORM
* [Docker](https://www.docker.com/) - Containerization

### Features
- Spring Boot 3.5 with Java 24
- Caffeine caching
- Frontend integration with Node.js build pipeline
- Docker containerization with Paketo Buildpacks

# How to install
## Prerequisites
- JDK 24 or later
- Gradle 9.0+
- Docker

**To pull the image from the command line, use:**
`$ docker pull ghcr.io/holly394/accountability:0f4eae4c`

## For Docker containerization configuration
To pull the image: `ghcr.io/holly394/accountability:master` <br>

You should configure the application based on my [application.yml](https://github.com/holly394/accountability-app-springboot/blob/master/src/main/resources/application.yml) file.

Then, make sure you map this from your host server to your container:<br>
	`/workspace/logs`<br>
	`/workspace/database`<br>
	`/workspace/application.yml`<br>
 
Set the container port to port `8080`<br>

See example here:<br>
```
services:  
  accountability-app:  
    container_name: accountability-app  
    image: ghcr.io/holly394/accountability:master  
    ports:  
      - "8080:8080"  
    volumes:  
      - /accountability-app/logs:/workspace/logs  
      - /accountability-app/database:/workspace/database  
      - /accountability-app/application.yml:/workspace/application.yml
```

# Database and log file configuration
### In your `application.yml`
See the full [application.yml](https://github.com/holly394/accountability-app-springboot/blob/master/src/main/resources/application.yml) example here. 

In this file, you can set the database URL, username, password, as well as the log level and file path for messages from this application.<br>
In my version, it's set to an H2 database, but you can connect yours to PostgreSQL or any other relational database service. <br>

### Gmail SMTP server setup
You can also set a [Gmail SMTP server](https://support.google.com/a/answer/176600?hl=en) in your `application.yml` so that you can send emails with password reset links if needed. <br>
Make sure to set up an [app password](https://support.google.com/mail/answer/185833?hl=en) for this. 

See example here: <br>
```
spring:
  mail:
    host: "smtp.gmail.com"
    port: 587
    username: "email address of the Gmail account you want to use"
    password: "the app password (not your real password!)"
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
```

# How to use (for users)
When first registering, each user starts out with a value of 0 in their wallet. <br>
You can start earning money and increase the value in your wallet by completing tasks and having your partners approve them.<br>
<br>
<img width="1467" height="567" alt="dashboard page" src="https://github.com/user-attachments/assets/c6573e05-2680-4cf6-9ebf-4efdefd6de90" /><br>

# Registration & resetting password
Users can sign in, reset their passwords if they forget them by having a reset token link sent to their email, and see their account information.<br>
<br>
<img width="669" height="476" alt="Log-in box" src="https://github.com/user-attachments/assets/5799ce2c-fb04-4449-8b4f-bdbb3919eb89" /><br>
<img width="886" height="552" alt="User information" src="https://github.com/user-attachments/assets/81573a0d-69a6-46aa-97f2-f4bee7c5a07c" /><br>

# Main Functionality: Tasks
Once you submit a new task, it will show up in your To-do list.<br>
<br>
<br>
<img width="1764" height="585" alt="task page overview" src="https://github.com/user-attachments/assets/c84fba78-3ae0-4b02-a4e7-45b8ec7a01e2" /><br>
From there, you can choose to either start or delete the task.<br><br>
Once started, the task will go into the "Tasks in progress" section where it will be time-tracked.<br><br>
<img width="1088" height="296" alt="task add and to do list" src="https://github.com/user-attachments/assets/0e089099-d8c8-4fed-8170-2b306170c16f" /><br>
The amount of time taken on each task is grouped by status and a total is calculated for how much the user could potentially "earn".<br><br>
However, earnings are added to the wallet only after a task has been completed and approved by a partner.<br><br>
Users can also see a list of their approved & rejected tasks, how much they've earned in their personal wallet, as well as recent tasks from their partners.<br><br>
<img width="1288" height="351" alt="tasks to do in progress completed" src="https://github.com/user-attachments/assets/688e5f5d-3ecb-48ce-85a9-05139faf0abf" /><br>


# Find partners
Users can find other existing users in the platform and request a partnership.<br>
<br>
<img width="1607" height="815" alt="partnership page tabs included" src="https://github.com/user-attachments/assets/04de4ee6-a260-4c4a-8867-69a86566f5b2" /><br>
<br>
Once a partnership is requested, users can either approve, reject, or delete the request based on whether they sent or received the request. <br>
<br>
<img width="850" height="462" alt="User search before request" src="https://github.com/user-attachments/assets/565406b1-658c-4b48-8e89-e7e8f2af6bfc" /><br>
<br>
Only requesters can delete a pending request while only senders of a rejection can undo a rejected partnership.<br>
<br>
<img width="807" height="878" alt="partnerships overview" src="https://github.com/user-attachments/assets/df689726-a5c5-44ec-b5a9-38131ca04198" />
<br>

# Wallet & purchases
Users can also make "purchases" using the earnings available in their wallets.<br>
<br>
<img width="1831" height="604" alt="purchase page overview" src="https://github.com/user-attachments/assets/dc426962-177f-41b7-8079-6e714e63881e" /><br>

# Graphs & visuals
Users can see a visual graph showing the top 5 approved tasks by time duration among them and their partners.<br>
<br>
<img width="551" height="595" alt="graph approved tasks" src="https://github.com/user-attachments/assets/9d426644-16cd-46dd-962d-0d7a586270ad" /><br>
<br>

They can also see a visual graph of wallet amounts among them and their partners.<br>
<br>
<img width="539" height="585" alt="wallet graph" src="https://github.com/user-attachments/assets/a624c70c-b2e7-4c38-b8c1-7d5a526a7b28" /><br>

