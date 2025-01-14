package assignments.ex2;
import javax.script.ScriptEngineManager;
import java.io.*;
// Add your documentation below:

public class Ex2Sheet implements Sheet {
    private Cell[][] table;
    // Add your code here
    private static final String ERR_OUT_OF_RANGE = "Error: Out of Range";

    // ///////////////////
    public Ex2Sheet(int x, int y) {
        table = new SCell[x][y];
        for(int i=0;i<x;i=i+1) {
            for(int j=0;j<y;j=j+1) {
                table[i][j] = new SCell(Ex2Utils.EMPTY_CELL);
            }
        }
        eval();
    }
    public Ex2Sheet() {
        this(Ex2Utils.WIDTH, Ex2Utils.HEIGHT);
    }

    @Override
    public String value(int x, int y) {
        String ans = Ex2Utils.EMPTY_CELL;
        // Add your code here
        if (!isIn(x, y)) {
            return ERR_OUT_OF_RANGE;
        }
        Cell c = get(x,y);
        if(c!=null) {ans = c.toString();}

        /////////////////////
        return ans;
    }

    @Override
    public Cell get(int x, int y) {
        return table[x][y];
    }

    @Override
    public Cell get(String cords) {
        Cell ans = null;
        // Add your code here
        try {
            // Extract column (letter) and row (number) from the input
            char column = Character.toUpperCase(cords.charAt(0));
            if (column < 'A' || column > 'Z') {
                throw new IllegalArgumentException("Invalid column: " + column);
            }
            int x = column - 'A'; // Convert column letter to 0-based index
            int y = Integer.parseInt(cords.substring(1)); // Extract the numeric part for the row

            // Check if the converted coordinates are valid
            if (isIn(x, y)) {
                ans = get(x, y);
            }
        } catch (Exception e) {
            // Handle any parsing or out-of-range exceptions
            System.err.println("Invalid coordinates: " + cords);
        }
        /////////////////////
        return ans;
    }

    @Override
    public int width() {
        return table.length;
    }
    @Override
    public int height() {
        return table[0].length;
    }
    @Override
    public void set(int x, int y, String s) {
        if (!isIn(x, y)) {
            System.err.println("Cannot set cell: Out of range.");
            return;
        }
        Cell c = new SCell(s);
        table[x][y] = c;
        System.out.println("Cell (" + x + ", " + y + ") set to: " + s);
        eval(); // Comment if causing issues during testing

        /////////////////////
    }
    @Override
    public void eval() {
        int[][] dd = depth();
        // Add your code here
        for (int i = 0; i < width(); i++) {
            for (int j = 0; j < height(); j++) {
                Cell cell = table[i][j];

                if (cell.getType() == Ex2Utils.FORM) {

                    try {
                        String formula = cell.getData().substring(1); // Remove the "="
                        String[] tokens = formula.split("(?=[-+*/()])|(?<=[-+*/()])");

                        // Replace cell references with their values
                        for (int k = 0; k < tokens.length; k++) {
                            tokens[k] = tokens[k].trim();

                            if (tokens[k].matches("[A-Z]+\\d+")) {
                                Cell refCell = get(tokens[k]);

                                if (refCell == null || refCell.getType() == Ex2Utils.FORM) {
                                    throw new IllegalArgumentException(Ex2Utils.ERR_FORM);
                                }
                                tokens[k] = refCell.getData(); // Replace with the value of the referenced cell
                            }
                        }

                        // Reconstruct the expression
                        StringBuilder expressionBuilder = new StringBuilder();
                        for (String token : tokens) {
                            expressionBuilder.append(token);
                        }
                        String expression = expressionBuilder.toString();

                        // Evaluate the expression
                        cell.setData(Double.toString(new Object() {
                            int pos = -1, ch;

                            void nextChar() {
                                ch = (++pos < expression.length()) ? expression.charAt(pos) : -1;
                            }

                            boolean eat(int charToEat) {
                                while (ch == ' ') nextChar();
                                if (ch == charToEat) {
                                    nextChar();
                                    return true;
                                }
                                return false;
                            }

                            double parse() {
                                nextChar();
                                double x = parseExpression();
                                if (pos < expression.length()) throw new RuntimeException("Unexpected: " + (char) ch);
                                return x;
                            }

                            double parseExpression() {
                                double x = parseTerm();
                                for (;;) {
                                    if (eat('+')) x += parseTerm(); // addition
                                    else if (eat('-')) x -= parseTerm(); // subtraction
                                    else return x;
                                }
                            }

                            double parseTerm() {
                                double x = parseFactor();
                                for (;;) {
                                    if (eat('*')) x *= parseFactor(); // multiplication
                                    else if (eat('/')) x /= parseFactor(); // division
                                    else return x;
                                }
                            }

                            double parseFactor() {
                                if (eat('+')) return parseFactor(); // unary plus
                                if (eat('-')) return -parseFactor(); // unary minus

                                double x;
                                int startPos = this.pos;
                                if (eat('(')) { // parentheses
                                    x = parseExpression();
                                    eat(')');
                                } else if ((ch >= '0' && ch <= '9') || ch == '.') { // numbers
                                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                                    x = Double.parseDouble(expression.substring(startPos, this.pos));
                                } else {
                                    throw new RuntimeException("Unexpected: " + (char) ch);
                                }

                                return x;
                            }
                        }.parse()));
                    } catch (Exception e) {
                        cell.setData(Ex2Utils.ERR_FORM); // Handle any evaluation errors
                    }
                }
            }
        }
        // ///////////////////
    }

    @Override
    public boolean isIn(int xx, int yy) {
        boolean ans = xx>=0 && yy>=0;
        // Add your code here
        ans = ans && xx < width() && yy < height();

        /////////////////////
        return ans;
    }

    @Override
    public int[][] depth() {
        int[][] ans = new int[width()][height()];
        // Add your code here
        for (int i = 0; i < width(); i++) {
            for (int j = 0; j < height(); j++) {
                Cell cell = table[i][j];
                if (cell.getType() == Ex2Utils.FORM) {
                    try {
                        ans[i][j] = calculateDepth(cell.getData(), this, new boolean[width()][height()]);
                    } catch (Exception e) {
                        ans[i][j] = Ex2Utils.ERR_CYCLE_FORM;
                    }
                } else {
                    ans[i][j] = 0; // Non-formula cells have a depth of 0
                }
            }
        }
        // ///////////////////
        return ans;
    }
    private int calculateDepth(String formula, Sheet sheet, boolean[][] visited) {
        if (formula.startsWith("=")) {
            formula = formula.substring(1);
        }
        String[] tokens = formula.split("(?=[-+*/()])|(?<=[-+*/()])");
        int maxDepth = 0;

        for (String token : tokens) {
            if (token.matches("[A-Z]+\\d+")) {

                Cell refCell = sheet.get(token);

                if (refCell == null) {
                    throw new IllegalArgumentException("Invalid cell reference: " + token);
                }

                int x = token.charAt(0) - 'A';

                int y = Integer.parseInt(token.substring(1));

                if (!isIn(x, y)) {
                    throw new IllegalArgumentException("Out of range: " + token);
                }

                if (visited[x][y]) {
                    throw new IllegalArgumentException("Cyclic dependency detected");
                }

                visited[x][y] = true; // Mark as visited
                int dependencyDepth = 1;

                if (refCell.getType() == Ex2Utils.FORM) {
                    dependencyDepth += calculateDepth(refCell.getData(), sheet, visited);
                }

                maxDepth = Math.max(maxDepth, dependencyDepth);
                visited[x][y] = false;
            }
        }

        return maxDepth;
    }

    @Override
    public void load(String fileName) throws IOException {
        // Add your code here
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        String line = reader.readLine(); // Skip header
        while ((line = reader.readLine()) != null) {

            String[] parts = line.split(",", 3);
            if (parts.length < 3) continue;
            int x = Integer.parseInt(parts[0]);
            int y = Integer.parseInt(parts[1]);

            String data = parts[2];
            set(x, y, data);
        }
        reader.close();
        /////////////////////
    }

    @Override
    public void save(String fileName) throws IOException {
        // Add your code here
        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
        writer.write("I2CS ArielU: SpreadSheet (Ex2) assignment\n");

        for (int i = 0; i < width(); i++) {
            for (int j = 0; j < height(); j++) {
                Cell cell = table[i][j];
                if (!cell.getData().equals(Ex2Utils.EMPTY_CELL)) {
                    writer.write(i + "," + j + "," + cell.getData() + "\n");
                }
            }
        }
        writer.close();
        /////////////////////
    }

    @Override
    public String eval(int x, int y) {
        String ans = null;
        if(get(x,y)!=null) {ans = get(x,y).toString();}
        // Add your code here
        Cell cell = get(x, y);
        if (cell.getType() == Ex2Utils.FORM) {
            String formula = cell.getData().substring(1); // Remove the leading '='


            try {
                double result = evaluateSimpleFormula(formula);
                ans = String.valueOf(result);
            } catch (Exception e) {
                ans = Ex2Utils.ERR_FORM; // Invalid formula
            }
        }
        /////////////////////
        return ans;
    }
    private double evaluateSimpleFormula(String formula) throws Exception {

        return Double.valueOf(new Object() {
            int pos = -1, ch;

            void nextChar() {
                ch = (++pos < formula.length()) ? formula.charAt(pos) : -1;
            }

            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();

                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            double parse() {
                nextChar();
                double x = parseExpression();
                if (pos < formula.length()) throw new RuntimeException("Unexpected: " + (char) ch);
                return x;
            }

            double parseExpression() {
                double x = parseTerm();
                for (;;) {
                    if      (eat('+')) x += parseTerm(); // addition
                    else if (eat('-')) x -= parseTerm(); // subtraction
                    else return x;
                }
            }

            double parseTerm() {
                double x = parseFactor();
                for (;;) {
                    if      (eat('*')) x *= parseFactor(); // multiplication
                    else if (eat('/')) x /= parseFactor(); // division
                    else return x;
                }
            }

            double parseFactor() {
                if (eat('+')) return parseFactor(); // unary plus
                if (eat('-')) return -parseFactor(); // unary minus

                double x;
                int startPos = this.pos;
                if (eat('(')) { // parentheses
                    x = parseExpression();
                    eat(')');

                } else if ((ch >= '0' && ch <= '9') || ch == '.') { // numbers
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    x = Double.parseDouble(formula.substring(startPos, this.pos));
                } else {
                    throw new RuntimeException("Unexpected: " + (char) ch);
                }

                return x;
            }
        }.parse());
    }

}
