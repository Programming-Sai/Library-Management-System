package org.ebenlib.utils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.ebenlib.cli.AuthHandler;
import org.ebenlib.cli.ConsoleUI;
import org.ebenlib.user.User;

public class FileUtil {

    /**
     * Read every line of the CSV at `path`, parse with `mapper`, return the list.
     */
    public static <T> List<T> readCSV(Path path, Function<String, T> mapper) {
        try {
            if (Files.notExists(path)) return new ArrayList<>();
            return Files.readAllLines(path, StandardCharsets.UTF_8).stream()
                    .filter(line -> !line.isBlank())
                    .map(mapper)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Failed to read CSV " + path + ": " + e.getMessage(), e);
        }
    }

    /**
     * Write `items` to CSV at `path`, one line per item via `toCsv`.
     * Overwrites existing file.
     */
    public static <T> void writeCSV(Path path, List<T> items, Function<T, String> toCsv) {
        try {
            List<String> lines = items.stream()
                                      .map(toCsv)
                                      .collect(Collectors.toList());
            Files.write(path, lines, StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write CSV " + path + ": " + e.getMessage(), e);
        }
    }

    public static void writeDemoData() {
        Path base = Path.of("app", "src", "main", "resources");
        try {
            Files.createDirectories(base);

            List<String> users = new ArrayList<>(List.of(
                "admin,admin,Librarian,true",
                "Mark Grayson,nolaN,Librarian,true",
                "Debby Grayson,nolan,Reader,true",
                "Oliver,thraxian bug,Reader,true",
                "Tom Cruise,1234,Reader,true",
                "Samantha West,password,Reader,true",
                "Daniel Black,wordpass,Reader,true",
                "Korra,kosmic,Reader,true",
                "Asami,engrx,Reader,true",
                "James Holden,rocinante,Reader,true",
                "Naomi Nagata,beltalowda,Reader,true",
                "Alex Kamal,thrust,Reader,true",
                "Amos Burton,shield,Reader,true",
                "Ciri,elderblood,Reader,true",
                "Geralt,witcher,Reader,true",
                "Yennefer,portal,Reader,true\n"
            
            ));


            Files.writeString(base.resolve("books.csv"), String.join("\n",
                "9780140328721,Matilda,Roald Dahl,Fiction,1988,Puffin,SH-A1,5",
                "9780439554930,Harry Potter and the Sorcerer's Stone,J.K. Rowling,Fantasy,1997,Scholastic,SH-A2,3",
                "9780061120084,To Kill a Mockingbird,Harper Lee,Classic,1960,Harper Perennial,SH-B1,4",
                "9780307277671,The Road,Cormac McCarthy,Post-Apocalyptic,2006,Vintage,SH-B2,2",
                "9781982137274,Where the Crawdads Sing,Delia Owens,Mystery,2018,G.P. Putnam's Sons,SH-C1,5",
                "9780439023528,The Hunger Games,Suzanne Collins,Dystopian,2008,Scholastic Press,SH-C2,4",
                "9781451673319,Fahrenheit 451,Ray Bradbury,Science Fiction,1953,Simon & Schuster,SH-C3,3",
                "9780140283334,1984,George Orwell,Dystopian,1949,Plume,SH-D1,6",
                "9780307387899,The Book Thief,Markus Zusak,Historical,2005,Knopf,SH-D2,3",
                "9780618260300,The Hobbit,J.R.R. Tolkien,Fantasy,1937,Houghton Mifflin,SH-D3,5",
                "9780141439600,Jane Eyre,Charlotte Bronte,Classic,1847,Penguin,SH-E1,2",
                "9780316769488,The Catcher in the Rye,J.D. Salinger,Fiction,1951,Little Brown,SH-E2,4",
                "9780142424179,Looking for Alaska,John Green,YA,2005,Speak,SH-E3,3",
                "9780590353427,Harry Potter and the Chamber of Secrets,J.K. Rowling,Fantasy,1998,Scholastic,SH-F1,3",
                "9780385472579,Zen and the Art of Motorcycle Maintenance,Robert M. Pirsig,Philosophy,1974,HarperTorch,SH-F2,1",
                "9780743273565,The Great Gatsby,F. Scott Fitzgerald,Classic,1925,Scribner,SH-F3,4",
                "9780553573404,A Game of Thrones,George R.R. Martin,Fantasy,1996,Bantam,SH-G1,2",
                "9780060256654,Where the Wild Things Are,Maurice Sendak,Children,1963,HarperCollins,SH-G2,5",
                "9780375842207,The Maze Runner,James Dashner,Dystopian,2009,Delacorte,SH-G3,3",
                "9780765326355,The Way of Kings,Brandon Sanderson,Fantasy,2010,Tor Books,SH-H1,2"
            ));;

            User currentUser = AuthHandler.getCurrentUser();
            boolean exists = users.stream().anyMatch(line -> line.split(",")[0].trim().equalsIgnoreCase(currentUser.getUsername()));
            if (!exists) {
                users.add(0, currentUser.getUsername() + "," + currentUser.getPassword() + "," + currentUser.getRole() + ",true"); // add after admin
            }

            Files.writeString(base.resolve("users.csv"), String.join("\n", users));

            Files.writeString(base.resolve("borrows.csv"), String.join("\n",
                "1,Tom Cruise,9780140328721,2025-05-12,APPROVED,2025-07-13,,0.0",
                "2,Tom Cruise,9780553573404,2025-07-12,RETURNED,2025-07-12,2025-07-12,0.0",
                "3,Tom Cruise,9780385472579,2025-07-12,REJECTED,2025-07-12,,0.0",
                "4,Tom Cruise,9780140283334,2025-06-01,APPROVED,2025-07-13,,0.0",
                "5,Debby Grayson,9780553573404,2025-07-12,APPROVED,2025-07-12,,0.0",
                "6,Debby Grayson,9780140328721,2025-07-12,PENDING,,,0.0",
                "7,Mark Grayson,9780439554930,2025-07-01,APPROVED,2025-07-02,,0.0",
                "8,Mark Grayson,9780141439600,2025-07-10,APPROVED,2025-07-12,,0.0",
                "9,Samantha West,9780439023528,2025-07-01,APPROVED,2025-07-05,,0.0",
                "10,Daniel Black,9780307387899,2025-06-20,RETURNED,2025-07-01,2025-07-01,0.0",
                "11,Korra,9780316769488,2025-07-02,PENDING,,,0.0",
                "12,Asami,9781451673319,2025-07-05,REJECTED,2025-07-06,,0.0",
                "13,James Holden,9780618260300,2025-07-11,APPROVED,2025-07-13,,0.0",
                "14,Naomi Nagata,9780141439600,2025-07-09,APPROVED,2025-07-13,,0.0",
                "15,Alex Kamal,9780307277671,2025-07-06,APPROVED,2025-07-14,,0.0",
                "16,Amos Burton,9780142424179,2025-07-08,APPROVED,2025-07-12,,0.0",
                "17,Ciri,9780141439600,2025-07-04,RETURNED,2025-07-10,2025-07-10,0.0",
                "18,Geralt,9780743273565,2025-07-07,APPROVED,2025-07-13,,0.0",
                "19,Yennefer,9780765326355,2025-07-03,APPROVED,2025-07-14,,0.0",
                "20,Tom Cruise,9780060256654,2025-07-12,PENDING,,,0.0",
                "21,Samantha West,9780375842207,2025-07-13,PENDING,,,0.0",
                "22,Oliver,9780439554930,2025-07-05,RETURNED,2025-07-10,2025-07-10,0.0",
                "23,Oliver,9780140283334,2025-07-11,APPROVED,2025-07-14,,0.0",
                "24,Debby Grayson,9780439023528,2025-07-13,PENDING,,,0.0",
                "25,Geralt,9780061120084,2025-07-02,REJECTED,2025-07-03,,0.0",
                "26,Yennefer,9780553573404,2025-07-01,APPROVED,2025-07-14,,0.0",
                "27,James Holden,9780142424179,2025-07-07,APPROVED,2025-07-12,,0.0",
                "28,Amos Burton,9781451673319,2025-07-05,RETURNED,2025-07-13,2025-07-13,0.0",
                "29,Naomi Nagata,9780618260300,2025-07-06,APPROVED,2025-07-14,,0.0",
                "30,Korra,9780439554930,2025-07-12,PENDING,,,0.0"
            ));

            // Don't touch session.csv during demo seeding
            // Files.writeString(base.resolve("session.csv"), "");
        } catch (IOException e) {
            throw new RuntimeException("Failed to write demo data: " + e.getMessage(), e);
        }
    }


    public static void loadFromFolder(File folder) {
        if (!folder.exists() || !folder.isDirectory()) {
           ConsoleUI.error("Invalid folder.");
           System.exit(0);
        }
        try {
            Path src = folder.toPath();
            Path dest = Path.of("app", "src", "main", "resources");

            Files.copy(src.resolve("books.csv"), dest.resolve("books.csv"), StandardCopyOption.REPLACE_EXISTING);
            Files.copy(src.resolve("users.csv"), dest.resolve("users.csv"), StandardCopyOption.REPLACE_EXISTING);
            Files.copy(src.resolve("borrows.csv"), dest.resolve("borrows.csv"), StandardCopyOption.REPLACE_EXISTING);
            // Files.copy(src.resolve("session.csv"), dest.resolve("session.csv"), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Failed to import data: " + e.getMessage(), e);
        }
    }


    public static void exportToFolder(File folder) {
        try {
            Path src = Path.of("app", "src", "main", "resources");
            Path dest = folder.toPath();
            Files.createDirectories(dest);

            Files.copy(src.resolve("books.csv"), dest.resolve("books.csv"), StandardCopyOption.REPLACE_EXISTING);
            Files.copy(src.resolve("users.csv"), dest.resolve("users.csv"), StandardCopyOption.REPLACE_EXISTING);
            Files.copy(src.resolve("borrows.csv"), dest.resolve("borrows.csv"), StandardCopyOption.REPLACE_EXISTING);
            // Files.copy(src.resolve("session.csv"), dest.resolve("session.csv"), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Failed to export data: " + e.getMessage(), e);
        }
    }


    public static boolean hasExistingData() {
        File dir = new File("resources");
        return new File(dir, "books.csv").exists() ||
               new File(dir, "users.csv").exists() ||
               new File(dir, "borrows.csv").exists();
    }

}
