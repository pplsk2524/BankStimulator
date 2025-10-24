# Banking Simulator

## Setup Instructions

1. Clone this repository
2. Copy `resources/config.properties.example` to `resources/config.properties`
3. Edit `config.properties` with your MySQL credentials:
```
   db.username=root
   db.password=YOUR_MYSQL_PASSWORD
```
4. Create database:
```sql
   CREATE DATABASE banking_simulator;
```
5. Run the SQL schema file
6. Build project: `mvn clean install`
7. Run: `mvn exec:java -Dexec.mainClass="com.banking.Main"`

## Configuration

**IMPORTANT:** Never commit `config.properties` file! It contains sensitive credentials.

All configuration is in `resources/config.properties` (not tracked by Git).
