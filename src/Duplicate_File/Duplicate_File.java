package Duplicate_File;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;

public class Duplicate_File {

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {

        Options ops = new Options();
        ops.addOption("i", "inputFolder", true, "Input Folders separated by ; ")
                .addOption("o", "output", true, "Output Folder for traces")
                .addOption("c", "deleteCommand", true, "Generate the deletion command lines");
        CommandLineParser clp = new DefaultParser();
        try {
            CommandLine cmd = clp.parse(ops, args);
            Properties params = new Properties();

            if (cmd.hasOption("i")) {
                params.put("inputFolder", cmd.getOptionValue("i"));
            }
            if (cmd.hasOption("o")) {
                params.put("outputFolder", cmd.getOptionValue("o"));
            }
            String StartTime = TimeStamp();
            String TraceFolder = "./";
            if (params.getProperty("outputFolder") != null) {
                TraceFolder = params.getProperty("outputFolder");
            }

            FileOutputStream Log = new FileOutputStream(TraceFolder + "\\" + StartTime + ".txt");
            PrintStream Traces = new PrintStream(Log);
            ArrayList<File> AllFiles = new ArrayList<File>();
            System.setOut(Traces);

            String InputsPaths = params.getProperty("inputFolder"); // "V:\\Films;\\\\192.168.1.19\\Patage_maison\\Films";
            List<String> paths = new ArrayList<String>();
            String[] items = InputsPaths.split(";");
            paths = Arrays.asList(items);

            for (String path : paths) {
                File file = new File(path);
                AllFiles.addAll(ListFile(file));
            }
            // Use MD5 algorithm
            MessageDigest md5Digest = MessageDigest.getInstance("MD5");

            HashMap<String, ArrayList<ArrayList<String>>> AllInfos = new HashMap<String, ArrayList<ArrayList<String>>>();

            for (File Fichier : AllFiles) {
                // Get the checksum
                String checksum = getFileChecksum(md5Digest, Fichier);
                ArrayList<String> FichierCourant = new ArrayList<String>();
                ArrayList<ArrayList<String>> FichiersCourants = new ArrayList<ArrayList<String>>();
                // see checksum

                if (AllInfos.get(checksum) == null) {
                    // Le checksum n'existe pas
                    FichierCourant.addAll(Arrays.asList(TimeStamp(), Long.toString(Fichier.lastModified()),
                            Long.toString(Fichier.length()), Fichier.getPath()));
                    FichiersCourants.add(FichierCourant);
                    // FichiersCourants.addAll(new
                    // ArrayList<String>().addAll(Arrays.asList(TimeStamp(),Long.toString(Fichier.lastModified()),Long.toString(Fichier.length()),Fichier.getPath())));
                    AllInfos.put(checksum, FichiersCourants);
                } else {
                    // Checksum exists
                    FichiersCourants = AllInfos.get(checksum);
                    FichierCourant.addAll(Arrays.asList(TimeStamp(), Long.toString(Fichier.lastModified()),
                            Long.toString(Fichier.length()), Fichier.getPath()));
                    FichiersCourants.add(FichierCourant);
                    AllInfos.replace(checksum, FichiersCourants);
                }
                // System.out.println(FichierCourant+"\t"+checksum);
            }

            Generate_Output(AllInfos, StartTime);

        } catch (org.apache.commons.cli.ParseException e) {
            // log.fatal("Unable to parse command. Please Check the documentation.");
        }

    }

    public static ArrayList<File> ListFile(File pathname) {

        ArrayList<File> files = new ArrayList<File>();
        if (pathname.isDirectory()) {

            for (File f : pathname.listFiles()) {
                if (!f.isHidden() && f.canRead()) {
                    // System.out.println(f);
                    files.addAll(ListFile(f));
                }
            }
        } else {
            files.add(pathname);
        }
        return files;
    }

    private static String getFileChecksum(MessageDigest digest, File file) throws IOException {
        // Get file input stream for reading the file content
        FileInputStream fis = new FileInputStream(file);

        // Create byte array to read data in chunks
        byte[] byteArray = new byte[1024 * 128];
        int bytesCount = 0;

        // Read file data and update in message digest
        try {
            while ((bytesCount = fis.read(byteArray)) != -1) {
                digest.update(byteArray, 0, bytesCount);
            }
            ;
        } catch (java.io.IOException e) {
            System.out.println(file.getName());
        }
        ;
        // close the stream; We don't need it now.
        fis.close();

        // Get the hash's bytes
        byte[] bytes = digest.digest();

        // This bytes[] has bytes in decimal format;
        // Convert it to hexadecimal format
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
        }

        // return complete hash
        return sb.toString();
    }

    public static String TimeStamp() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd-HH_mm_ss");
        Date today = Calendar.getInstance().getTime();
        return dateFormat.format(today);
    }

    public static void Generate_Output(HashMap<String, ArrayList<ArrayList<String>>> Map, String Time) {
        long SaveSpace = 0;
        System.out.println("############### File report : ###############");
        System.out.println("Starting Time : " + Time);
        System.out.println("End Time : " + TimeStamp());
        System.out.println("Handled files : " + Map.size());

        int j = 1;
        for (Map.Entry<String, ArrayList<ArrayList<String>>> entry : Map.entrySet()) {
            if (entry.getValue().size() > 1) {
                System.out.print("## File familly : " + j);
                System.out.println("\t Hash Key : " + entry.getKey());
                System.out.println("Size : " + Integer.parseInt(entry.getValue().get(0).get(2)) / 1024 + "Ko");
                SaveSpace = SaveSpace + Integer.parseInt(entry.getValue().get(0).get(2));

                System.out.println("Identical files : ");

                for (int i = 0; i < entry.getValue().size(); i++) {
                    System.out.println(entry.getValue().get(i).get(3) + " ");
                }
                j++;
            }
        }
        System.out.println("############### SAVED SPACE : ###############");
        System.out.println(SaveSpace / (1024 ^ 2));

    }

}