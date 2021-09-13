package bgu.spl.net.srv;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Passive object representing the Database where all courses and users are stored.
 * <p>
 * This class must be implemented safely as a thread-safe singleton.
 * You must not alter any of the given public methods of this class.
 * <p>
 * You can add private fields and methods to this class as you see fit.
 */
public class Database {

    private final static String path = "/home/spl211/Desktop/SPL/Assignment3/Assignment3/Server/Courses.txt";
    private ConcurrentHashMap<Integer, Course> courses; // courses: CourseID - {CourseName, List(kdam), MaxSits, AvailableSits, List(Students Registered)}
    private ConcurrentHashMap<String, Student> students; // students: StudentUser - {Status, Password, List(registeredCourses(Integer))}
    private ConcurrentHashMap<String, Admin> admins; // admins: AdminUser - {Status, Password}
    private Vector<Integer> courseIndex; // the course index in text to number to courseID

    //to prevent user from creating new Database
    private static class InstanceHolder {
        private static Database singleton = new Database();
    }

    private Database() {
        courses = new ConcurrentHashMap<>();
        students = new ConcurrentHashMap<>();
        admins = new ConcurrentHashMap<>();
        courseIndex = new Vector<>();
        this.initialize(path);
    }

    /**
     * Retrieves the single instance of this class.
     */
    public static Database getInstance() {
        return InstanceHolder.singleton;
    }


    /**
     * loads the courses from the file path specified
     * into the Database, returns true if successful.
     *
     * @param coursesFilePath the path provided
     * @return - true if loaded successfully, otherwise false.
     */
    public boolean initialize(String coursesFilePath) {
        BufferedReader reader;
        try {
            /* reading the courses file */
            reader = new BufferedReader(new FileReader(coursesFilePath));
            String line = reader.readLine();
            while (line != null) { // there is a course in the file
                String[] params = line.split("\\|"); // params = {CourseNum, CourseName, List<Course< kdams, MaxSeats}
                Integer CourseNumber = Integer.parseInt(params[0]);
                this.courseIndex.add(CourseNumber);
                String CourseName = params[1];
                String[] kdams = params[2].substring(1, params[2].length() - 1).split(",");
                LinkedList<Integer> kdamsInts = new LinkedList<>();
                if (!kdams[0].equals(""))
                    for (String kdam : kdams)
                        kdamsInts.add(Integer.parseInt(kdam));
                Integer maxSeats = Integer.parseInt(params[3]);
                courses.put(CourseNumber, new Course(CourseNumber, CourseName, kdamsInts, maxSeats));
                line = reader.readLine();
            }
            reader.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * This method registers an admin this Database class.
     *
     * @param admin requesting admin to register
     * @param pass  the admins password
     * @throws IllegalCallerException if one of the following:
     *                                1. An already registered student tries to register as an admin.
     *                                2. The admins tries to register itself twice
     */
    public void adminReg(String admin, String pass) throws IllegalCallerException {
        /* OPCODE 1*/
            if (students.containsKey(admin))
                throw new IllegalCallerException("student tried to register as an admin");
            else if (admins.containsKey(admin))
                throw new IllegalCallerException("same admin tried to register twice");
            else {
                admins.put(admin, new Admin(admin, pass));
            }
    }

    /**
     * This method registers a a student in this Database class.
     *
     * @param student requesting student to register.
     * @param pass    the provided student password.
     * @throws IllegalCallerException if one of the following:
     *                                1. An already registered admin tries to register as an student.
     *                                2. The student tries to register itself twice
     */
    public void studentReg(String student, String pass) throws IllegalCallerException {
        /* OPCODE 2 */
            if (admins.containsKey(student))
                throw new IllegalCallerException("admin tried to register as an student");
            else if (students.containsKey(student))
                throw new IllegalCallerException("same student tried to register twice");
            else {
                students.put(student, new Student(student, pass));
            }
    }

    /**
     * This method logins an already registered user.
     *
     * @param user that wishes to login.
     * @param pass the provided password for the user.
     * @throws IllegalCallerException if one of the following:
     *                                1. An already logged in user tries to login.
     *                                2. The user's provided password doesn't match the password in this {{@link #Database()}}.
     */
    public void login(String user, String pass) {
        /* OPCODE 3 */
        if (admins.containsKey(user))
            adminLog(user, pass);
        else if (students.containsKey(user))
            studentLog(user, pass);
        else
            throw new IllegalCallerException("the user is not registered so cant login");
    }


    /**
     * This method will log in an existing student in the this Database class.
     *
     * @param student requesting student to login.
     * @param pass    the requesting students provided password.
     * @throws IllegalCallerException if one of the following:
     *                                1. the student is not registered so cant login.
     *                                2. logging student password doesn't match.
     *                                3. a student tried to log in twice
     */
    private void studentLog(String student, String pass) throws IllegalCallerException {
        Student loginStudent = students.get(student);
        if (!loginStudent.getPass().equals(pass)) // if the password doesnt match
            throw new IllegalCallerException("logging student password doesn't match");
        else if (loginStudent.getStatus())
            throw new IllegalCallerException("a student tried to log in twice");
            /* everything went fine */
        else
            loginStudent.setStatus(true);
    }


    private void adminLog(String admin, String pass) throws IllegalCallerException {
        Admin loginAdmin = admins.get(admin);
        if (!loginAdmin.getPass().equals(pass)) // if the password doesnt match
            throw new IllegalCallerException("logging admin password doesnt match");
        else if (loginAdmin.getStatus()) // the admin is already logged in
            throw new IllegalCallerException("an admin tried to log in twice");
            /* everything went fine */
        else
            loginAdmin.setStatus(true);
    }

    public void logout(String user) {
        /* OPCODE 4 */
        if (students.containsKey(user)) { // the user is a student
            Student student = students.get(user);
            if (!student.getStatus())
                throw new IllegalCallerException("The student is already logged out");
            else
                student.setStatus(false);
        } else if (admins.containsKey(user)) { // the user is an admin
            Admin admin = admins.get(user);
            if (!admin.getStatus())
                throw new IllegalCallerException("The admin is already logged out");
            else
                admin.setStatus(false);
        } else { // the user does not exists
            throw new IllegalArgumentException("user does not exists");
        }
    }

    /*note that the server needs to provide this method who is the student from the client info and the client wont provide it.*/
    public void courseReg(String name, Integer courseNum) {
        /* OPCODE 5 */
        if (!students.containsKey(name) ||
                !students.get(name).getStatus() /* student logged out */ ||
                !courses.containsKey(courseNum) ||
                courses.get(courseNum).getAvailableSeats().equals(0) ||
                courses.get(courseNum).studentsRegistered.contains(students.get(name)))
            throw new IllegalArgumentException("course doesn't exists or the student isn't registered or logged in or no available sits or the student is alrady registered");

        Student student = students.get(name);
        Course course = courses.get(courseNum);
        LinkedList<Integer> studentCourses = student.getCourses();

        if ((studentCourses.containsAll(course.getKdam())) && !studentCourses.contains(course.getNumber())) { /* student got all kdams and not trying to register to an already registered course*/
            student.addCourse(course);// adding the course to the students course list
            course.addStudent(student); // adding the student - this method will also decrease the number of available seats.
        } else
            throw new IllegalCallerException("the student does not have the required courses for this course - " + courseNum);
    }

    //note that the server needs to provide this method who is the student from the client info and the client wont provide it.
    public LinkedList<Integer> kdamCheck(String name, Integer courseNum) {
        /* OPCODE 6 */
        if (!students.containsKey(name) || !students.get(name).getStatus() || !courses.containsKey(courseNum))
            throw new IllegalArgumentException("course doesn't exists or the student isn't registered or logged in");
        Course course = courses.get(courseNum);
        LinkedList<Integer> kdam = course.getKdam();
        LinkedList<Integer> kdamOut = new LinkedList<>();
        /* building a list of the kdam courses by the wanted order */
        for (Integer courseID : courseIndex) //TODO: maybe change to quicksort by the index
            for (Integer c : kdam)
                if (courseID.equals(c))
                    kdamOut.add(courseID);
        return kdamOut;
    }

    /**
     * This method will get the states of a requesting course.
     *
     * @param name      the user requesting the {@link Course} states.
     * @param courseNum the number of the {@link Course}.
     * @return A string with the course states or will throw an exception.
     * @throws IllegalArgumentException
     */
    public String courseStat(String name, Integer courseNum) {
        if (!admins.containsKey(name) || !admins.get(name).getStatus() || !courses.containsKey(courseNum))
            throw new IllegalArgumentException("course doesn't exists or the admin isn't registered or logged in");
        return courses.get(courseNum).toString();
    }

    public String studentStat(String admin, String name) {
        /* OPCODE: 8 */
        if (!admins.containsKey(admin) ||
                !admins.get(admin).getStatus() ||
                !students.containsKey(name))
            throw new IllegalArgumentException("student doesn't exists or the admin isn't registered or logged in");
        String out = "Student: " + name + "\n" +
                "Courses: [";
        Student student = students.get(name);
        LinkedList<Integer> StudentCourses = student.getCourses();

        boolean added = false;
        for (Integer courseID : courseIndex) //TODO: maybe change to quicksort by the index
            for (Integer course : StudentCourses)
                if (courseID.equals(course)) {
                    out += course.toString() + ",";
                    added = true;
                }
        if (added)
            out = out.substring(0, out.length() - 1); // removing the last ","
        out += "]";
        return out;
    }

    public String isRegistered(String student, Integer course) {
        /* OPCODE: 9 */
        if (!students.containsKey(student) || !students.get(student).getStatus() || !courses.containsKey(course))
            throw new IllegalArgumentException("student doesn't exists or the admin isn't registered or logged in or course doesn't exists");
        if (students.get(student).getCourses().contains(course) && courses.get(course).studentsRegistered.contains(students.get(student))) //checking the course is inside the student and the student is inside the course
            return "REGISTERED";
        else
            return "NOT REGISTERED";
    }

    public void unregister(String name, Integer courseNum) {
        /* OPCODE: 10 */
        if (!students.containsKey(name) || !students.get(name).getStatus() || !courses.containsKey(courseNum))
            throw new IllegalArgumentException("course doesn't exists or the student isn't registered or logged in");
        Student student = students.get(name);
        LinkedList<Integer> StudentCourses = student.getCourses();
        Course course = courses.get(courseNum);
        if (!StudentCourses.contains(courseNum))
            throw new IllegalArgumentException("the student is not registered to this course: " + courseNum);
        student.removeCourse(courses.get(courseNum));
        course.removeStudent(students.get(name));
    }

    public String myCourses(String name) {
        /* OPCODE: 11 */
        if (!students.containsKey(name) || !students.get(name).getStatus())
            throw new IllegalArgumentException("course doesn't exists or the student isn't registered or logged in");
        LinkedList<Integer> StudentCourses = students.get(name).getCourses();
        return StudentCourses.toString().replaceAll(" ", "");
    }

    public boolean loggedin(String user) {
        if (admins.containsKey(user))
            return admins.get(user).getStatus();
        else if (students.containsKey(user))
            return students.get(user).getStatus();
        else
            return false;
    }

    private class Course {
        /* ----------  fields ----------- */

        private Integer number;
        private String name;
        private LinkedList<Integer> kdam;
        private LinkedList<Student> studentsRegistered;
        private Integer maxSeats;
        private AtomicInteger availableSeats;


        /* ----------- Constructor ----------- */

        public Course(Integer _number, String _name, LinkedList<Integer> _kdam, Integer _maxSeats) {
            this.number = _number;
            this.name = _name;
            this.kdam = _kdam;
            this.maxSeats = _maxSeats;
            this.availableSeats = new AtomicInteger(_maxSeats);
            this.studentsRegistered = new LinkedList<>();

        }

        public Integer getNumber() {
            return number;
        }

        public String getName() {
            return name;
        }

        public LinkedList<Integer> getKdam() {
            return kdam;
        }

        public Integer getMaxSeats() {
            return maxSeats;
        }

        public Integer getAvailableSeats() {
            return availableSeats.get();
        }

        public LinkedList<Student> getStudentsRegistered() {
            return studentsRegistered;
        }

        public void addStudent(Student student) {
            studentsRegistered.add(student);
            availableSeats.decrementAndGet();
        }

        public void removeStudent(Student student) {
            studentsRegistered.remove(student);
            availableSeats.incrementAndGet();
        }

        public void addKdam(Course kdam) {
            this.kdam.add(kdam.getNumber());
        }

        public String toString() {
            String out = "";
            String courseName = this.getName();
            Integer availableSeats = this.getAvailableSeats();
            LinkedList<Student> studentsRegistered = this.getStudentsRegistered();
            studentsRegistered.sort(Student::compareTo);

            out += ("Course: (" + this.getNumber() + ") " + courseName + "\n" +
                    "Seats Available: " + availableSeats + "/" + this.getMaxSeats() + "\n" +
                    "Students Registered: " + studentsRegistered);
            return out;
        }
    }

    private class Student implements Comparable {

        /* ----------  fields ----------- */

        private String name;
        private String pass;
        private boolean status;
        private LinkedList<Integer> studentCourses;

        /* ----------- Constructor ----------- */

        public Student(String _name, String _pass) {
            this.name = _name;
            this.pass = _pass;
            this.status = false;
            this.studentCourses = new LinkedList<>();

        }

        public String getName() {
            return name;
        }

        public String getPass() {
            return pass;
        }

        public boolean getStatus() {
            return status;
        }

        public LinkedList<Integer> getCourses() {
            return studentCourses;
        }

        public void setStatus(boolean status) {
            this.status = status;
        }

        public void addCourse(Course course) {
            studentCourses.add(course.getNumber());
        }

        public void removeCourse(Course course) {
            this.studentCourses.remove(course.getNumber());
        }

        @Override
        public int compareTo(Object o) {
            return this.getName().compareTo(((Student) o).getName());
        }

        public String toString() {
            return this.getName();
        }
    }

    private class Admin {

        private String name;
        private String pass;
        private boolean status;

        public Admin(String _name, String _pass) {
            this.name = _name;
            this.pass = _pass;
            status = false;
        }

        public String getName() {
            return name;
        }

        public String getPass() {
            return pass;
        }

        public boolean getStatus() {
            return status;
        }

        public void setStatus(boolean status) {
            this.status = status;
        }
    }
}





