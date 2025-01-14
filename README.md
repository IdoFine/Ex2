This project implements a simple spreadsheet application with a graphical user interface (GUI).
The spreadsheet supports storing and evaluating text, numbers, and formulas. Users can interact with the grid to edit cells, input data, and observe calculations dynamically.
Features- 
Cell Types: Text, Numbers, formulas(must start with "=")
Dynamic Evaluation: Formulas are automatically evaluated based on cell references and mathematical operators. Error handling for invalid formulas or cyclic dependencies.
File Operations: Save and load spreadsheet data from a file.
Classes Overview-
Ex2Sheet: Implements the Sheet interface, managing a grid of SCell objects. Includes methods for setting and evaluating cell data, detecting cyclic dependencies, and managing file operations.
Ex2GUI: Handles the graphical user interface. Provides functionalities to interact with the spreadsheet, visualize data, and manage input/output.
Ex2Utils: A utility class containing constants and helper methods, including predefined colors, default grid size, and error messages.

