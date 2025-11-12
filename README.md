# Banking Transaction Simulator

The **Banking Transaction Simulator** is a Java-based application designed to simulate basic banking operations in a sandbox environment. It enables users to manage accounts, perform deposits, withdrawals, transfers, and view transaction reports — helping to understand how a simple digital banking platform can work end-to-end.

## Objective
This project aims to create a basic banking transaction simulator using core Java features, including exception handling for error management, collections for account storage, and JDBC for transaction logging. Integrated with text files (simulating Google Sheets) and email APIs for balance alerts, the system will process deposits, withdrawals, and transfers while maintaining account balances. This tool will help users understand banking operations, ensure transaction accuracy, and generate simple reports without advanced financial libraries or AI.

## Features  
### Account Management  
- Create new bank accounts  
- Query account balances  

### Transaction Processing  
- Deposit funds to accounts  
- Withdraw funds from accounts  
- Transfer funds between accounts  
- All transactions update balances in real time  

### Error Handling  
- Prevents invalid operations (for example, overdrafts, invalid account numbers)  
- Ensures data integrity during transactions  

### Database Integration  
- Uses JDBC to connect to a MySQL database  
- Stores account and transaction data persistently  

### Report Generation  
- Basic reports covering transaction history, account summaries  

### Email Notifications  
- Sends alerts (for example: low balance notifications) via email  


## Setup Instructions  
### 1. Clone the repository  
```bash
git clone https://github.com/pplsk2524/BankStimulator.git
cd BankStimulator
```
### 2.Configuraton
Copy the example configuration file:
```bash
cp resources/config.properties.example resources/config.properties
```
Edit resources/config.properties and fill in your MySQL credentials and email configuration:
```bash
db.username=YOUR_MYSQL_USERNAME  
db.password=YOUR_MYSQL_PASSWORD  
# Also configure email settings (SMTP host, port, user, password) as required
```

### 3. Database Setup

In MySQL, create the database:
```bash
CREATE DATABASE banking_simulator;
```

Run the SQL schema file found in the SQL schema/ directory (or whichever path the schema is in) to create tables and constraints.

### 4. Build the project

Use Maven to build:
```bash
mvn clean install
```
### 5. Run the application

Execute the main class with Maven:
```bash
mvn exec:java -Dexec.mainClass="com.banking.Main"
```
## Project Structure
``` bash
BankStimulator/
│
├── src/main/                # Java source code  
│   └── com/banking/         # Core banking packages  
│
├── resources/               # Configuration files  
│   ├── config.properties     # (not committed)  
│   └── config.properties.example  
│
├── SQL schema/              # Database table definitions and setup scripts  
│
├── pom.xml                  # Maven project file  
│
└── README.md                # Project documentation  
```
## Requirements

- Java JDK 8 or higher

- MySQL database

- Maven build tool

## Important Notes

- Do not commit your config.properties file — it contains sensitive credentials.

- This project is for learning and demonstration purposes only.

- You are encouraged to extend and adapt the simulator (for example adding a REST API, GUI, or richer business logic) for your own experimentation.

## Author

Palakurthi Poojitha

GitHub: pplsk2524

Email: pplsk2004@gmail.com

## License

This project is licensed under the MIT License. See the LICENSE file for details.

