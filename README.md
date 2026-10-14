# Library Management System Mini Project

**Name:** Gary Munamba  
**Registration Number:** H250094f  
**Department:** School of Information and Sciences  
**Assignment:** Library Management System Mini Project

## Overview

A Java console application demonstrating encapsulation, inheritance, abstraction, polymorphism, interfaces, custom exceptions, collections, streams, and CSV persistence. Books have a 21-day loan period and DVDs have a 7-day loan period. Members may have at most three active loans.

## Requirements

- JDK 8 or later
- No third-party dependencies

## Compile and run

From the project root, run:

```powershell
javac -d out src\library\*.java
java -cp out library.LibraryApp
```

The application creates a `data` directory in the current working directory and saves `items.csv`, `members.csv`, and `loans.csv` when you select **0. Save and exit**. Existing CSV data is loaded when the application starts. Keep the CSV files together and do not edit their header rows.

## Menu

1. Add a book or DVD
2. Register a member
3. Borrow an item
4. Return an item
5. List overdue items
6. Search items by title (case-insensitive, using streams)
7. List all items
8. List all members and their active loans
0. Save and exit

## Persistence format

- `items.csv`: type, ID, title, and book author or DVD runtime in minutes
- `members.csv`: member ID and name
- `loans.csv`: item ID, member ID, and original loan date for each active loan

CSV fields are quoted and escaped, so titles and names may contain commas and quotes.

## Project report

See [REPORT.md](REPORT.md) for the design and implementation summary and [CLASS_DIAGRAM.md](CLASS_DIAGRAM.md) for the class diagram.


