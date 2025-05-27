public class FileSlot {
    // This class represents a file slot in the file server.

    private final int slotNumber;
    private String fileName;
    private byte[] fileContent;
    private boolean isEmpty;

    public FileSlot(int slotNumber) {
        this.slotNumber = slotNumber;
        this.isEmpty = true;
    }

    public boolean isEmpty() {
        return isEmpty;
    }

    public void storeFile(String fileName, byte[] fileContent) {
        this.fileName = fileName;
        this.fileContent = fileContent;
        this.isEmpty = false;
    }

    public void clear() {
        this.fileName = null;
        this.fileContent = null;
        this.isEmpty = true;
    }

    public String getFileName() { return fileName; }
    public byte[] getFileContent() { return fileContent; }
    public int getSlotNumber() { return slotNumber; }
}
