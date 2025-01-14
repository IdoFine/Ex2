package assignments.ex2;
// Add your documentation below:

public class SCell implements Cell {
    private String line; // Data of the cell
    private int type;    // Type of the cell (FORM, NUMBER, TEXT, etc.)
    private int order;

    // Constructor
    public SCell(String s) {
        setData(s); // Initialize the cell's data and type
    }

    @Override
    public int getOrder() {
        return order;
    }

    @Override
    public void setOrder(int t) {
        order = t;
    }

    @Override
    public String toString() {

        return getData();
    }

    @Override
    public void setData(String s) {

        line = s; // Store the input data


        if (line.startsWith("=")) {
            setType(Ex2Utils.FORM);
        } else {
            try {
                Double.parseDouble(line);
                setType(Ex2Utils.NUMBER);
            } catch (NumberFormatException e) {
                setType(Ex2Utils.TEXT);
            }
        }
        System.out.println("SCell setData: " + line + " (type: " + type + ")");

    }

    @Override
    public String getData() {
        return line;
    }

    @Override
    public int getType() {
        return type;
    }

    @Override
    public void setType(int t) {
        type = t;
    }
}
