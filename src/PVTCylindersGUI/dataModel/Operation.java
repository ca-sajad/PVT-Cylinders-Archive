package PVTCylindersGUI.dataModel;

public enum Operation {
    SERVICE("Service"),         // when cylinder is emptied of the sample or when a change is made in valves, o-rings, ...
    NEW_SAMPLE("New Sample"),   // during sampling, making new samples including recombination
    TEST("Test"),               // all tests, including volume taken to create new or recombined samples
    INSPECTION("Inspection"),   // checking for leaks
    ;

    private String string;

    // constructor to set the string
    Operation(String name){string = name;}

    @Override
    public String toString() {
        return string;
    }
}
