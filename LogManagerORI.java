import java.awt.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class LogManagerORI2 {
    private BufferedWriter writer;         // [UPDATED] non-static writer
    private File currentLogFile;           // [UPDATED] non-static file
    private String baseFolder;             // For dynamic folder path

    // ================================
    // Constructors
    // ================================
    public LogManagerORI2() {
        this.baseFolder = "Logs";          // Default folder
    }

    public LogManagerORI2(String folderName) {
        if (folderName == null || folderName.trim().isEmpty()) {
            this.baseFolder = "Logs";
        } else {
            this.baseFolder = folderName.trim();
        }
    }

    // ================================
    // Initialize and create folder structure for logs
    // ================================
    public void initializeLog() {
        try {
            Date now = new Date();

            // Format year, month, date, time for folder and file
            SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
            SimpleDateFormat monthFormat = new SimpleDateFormat("MMM");
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd");
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH-mm-ss");

            String year = yearFormat.format(now);
            String month = monthFormat.format(now);
            String date = dateFormat.format(now);
            String time = timeFormat.format(now);

            // Folder structure: baseFolder/year/month/date/
            String folderPath = baseFolder + "/" + year + "/" + month + "/" + date;
            File folder = new File(folderPath);
            if (!folder.exists()) {
                folder.mkdirs();
                System.out.println("[LOG] Created folders: " + folderPath);
            }

            // Create timestamped log file
            String fileName = "log_" + time + ".txt";
            File logFile = new File(folderPath, fileName);
            logFile.createNewFile();

            currentLogFile = logFile;

            // Open writer
            writer = new BufferedWriter(new FileWriter(logFile, true));

            log("[SYSTEM] Log initialized at " + now);
            System.out.println("[LOG] Log file created: " + logFile.getAbsolutePath());

        } catch (IOException e) {
            System.err.println("Error initializing log: " + e.getMessage());
        }
    }

    // ================================
    // Write a log entry with timestamp
    // ================================
    public void log(String message) {
        try {
            if (writer == null) initializeLog();
            String timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
            writer.write("[" + timestamp + "] " + message + "\n");
            writer.flush();
        } catch (IOException e) {
            System.err.println("Error writing to log: " + e.getMessage());
        }
    }

    // ================================
    // Close the log safely
    // ================================
    public void closeLog() {
        try {
            if (writer != null) {
                log("[SYSTEM] Log closed.");
                writer.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing log: " + e.getMessage());
        }
    }

    // ================================
    // Archive current log file
    // ================================
    public void archiveLog() {
        if (currentLogFile == null) {
            System.out.println("No log file to archive.");
            return;
        }

        try {
            File parent = currentLogFile.getParentFile(); // points to date folder
            File monthFolder = parent.getParentFile();
            File yearFolder = monthFolder.getParentFile();

            // Create same structure inside Archive
            String archiveFolderPath = baseFolder + "/Archive/" + yearFolder.getName() + "/" + monthFolder.getName() + "/" + parent.getName();
            File archiveDir = new File(archiveFolderPath);
            if (!archiveDir.exists()) archiveDir.mkdirs();

            File destFile = new File(archiveDir, currentLogFile.getName());

            try (FileInputStream in = new FileInputStream(currentLogFile);
                 FileOutputStream out = new FileOutputStream(destFile)) {

                byte[] buffer = new byte[1024];
                int length;
                while ((length = in.read(buffer)) > 0) {
                    out.write(buffer, 0, length);
                }
            }

            System.out.println("[LOG] Archived file to: " + destFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Error archiving log: " + e.getMessage());
        }
    }

    // ================================
    // Delete a log file by user input
    // ================================
    public void deleteLog() {
        Scanner sc = new Scanner(System.in);

        System.out.print("Enter log date path (e.g., 2025/Oct/28): ");
        String datePath = sc.nextLine();

        System.out.print("Enter log file name (e.g., log_12-34-56.txt): ");
        String fileName = sc.nextLine();

        File logFile = new File(baseFolder + "/" + datePath + "/" + fileName);

        if (logFile.exists()) {
            if (logFile.delete()) {
                System.out.println("[LOG] File deleted successfully: " + logFile.getAbsolutePath());
            } else {
                System.out.println("[ERROR] Failed to delete file.");
            }
        } else {
            System.out.println("[ERROR] File not found: " + logFile.getAbsolutePath());
        }
    }

    // ================================
    // Move a log file to another folder
    // ================================
    public void moveLog() {
        Scanner sc = new Scanner(System.in);

        System.out.print("Enter log date path (e.g., 2025/Oct/28): ");
        String datePath = sc.nextLine();

        System.out.print("Enter log file name (e.g., log_12-34-56.txt): ");
        String fileName = sc.nextLine();

        File sourceFile = new File(baseFolder + "/" + datePath + "/" + fileName);
        if (!sourceFile.exists()) {
            System.out.println("[ERROR] Source file not found: " + sourceFile.getAbsolutePath());
            return;
        }

        System.out.print("Enter destination folder path (e.g., Logs/Archive/2025/Oct/28): ");
        String destinationPath = sc.nextLine();

        File destinationFolder = new File(destinationPath);
        if (!destinationFolder.exists()) {
            destinationFolder.mkdirs();
            System.out.println("[LOG] Created destination folder: " + destinationFolder.getAbsolutePath());
        }

        File destinationFile = new File(destinationFolder, fileName);

        try (FileInputStream in = new FileInputStream(sourceFile);
             FileOutputStream out = new FileOutputStream(destinationFile)) {

            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }

            if (sourceFile.delete()) {
                System.out.println("[LOG] File moved successfully to: " + destinationFile.getAbsolutePath());
            } else {
                System.out.println("[WARN] File copied but not deleted from source.");
            }

        } catch (IOException e) {
            System.err.println("Error moving file: " + e.getMessage());
        }
    }

    // ================================
    // Main Class (Program Entry Point)
    // ================================
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        // [ADDED] Multiple log managers using constructor
        LogManagerORI2 agvLog = new LogManagerORI2("Logs/AGV");           // [ADDED]
        LogManagerORI2 batteryLog = new LogManagerORI2("Logs/Battery");   // [ADDED]
        LogManagerORI2 systemLog = new LogManagerORI2("Logs/System");     // [ADDED]

        // Step 1: Start logging system
        systemLog.initializeLog();    // system log
        agvLog.initializeLog();       // AGV log
        batteryLog.initializeLog();   // Battery log

        // Step 2: Example workflow
        systemLog.log("[INFO] Warehouse automation simulation started.");
        agvLog.log("[AGV] AGV#1 picked up Box#A1 from Entry point.");
        agvLog.log("[AGV] AGV#1 moved to shelf position [2,2].");
        agvLog.log("[AGV] Box#A1 stored successfully.");
        batteryLog.log("[BATTERY] AGV#1 battery at 50% — recharging.");
        batteryLog.log("[BATTERY] Recharging complete, back to 100%.");
        systemLog.log("[SYSTEM] All systems normal.");

        // Step 3: End of program
        systemLog.log("[INFO] Simulation completed successfully.");
        systemLog.closeLog();
        agvLog.closeLog();
        batteryLog.closeLog();

        System.out.println("\nSimulation completed. Check 'Logs' folder for output log files.");

        // [KEEP MAIN INTERACTIONS SAME]
        System.out.print("Do you want to view a previous log file? (y/n): ");
        if (sc.nextLine().equalsIgnoreCase("y")) {
            System.out.print("Enter the date (YYYY/MMM/dd): ");
            String datePath = sc.nextLine();  // e.g. "2025/Oct/23"
            File logFolder = new File("logs/" + datePath);
            if (logFolder.exists()) {
                File[] files = logFolder.listFiles((dir, name) -> name.endsWith(".txt"));
                if (files != null && files.length > 0) {
                    try {
                        Desktop.getDesktop().open(files[files.length - 1]); // opens latest log
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else System.out.println("No log files found for that date.");
            } else System.out.println("No folder found for that date.");
        }

        System.out.print("\nDo you want to delete a log file? (y/n): ");
        if (sc.nextLine().equalsIgnoreCase("y")) {
            systemLog.deleteLog();
        }

        System.out.print("\nDo you want to move a log file? (y/n): ");
        if (sc.nextLine().equalsIgnoreCase("y")) {
            systemLog.moveLog();
        }
    }
}
