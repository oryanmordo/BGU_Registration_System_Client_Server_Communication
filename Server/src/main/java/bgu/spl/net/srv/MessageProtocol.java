package bgu.spl.net.srv;

import bgu.spl.net.api.MessagingProtocol;

import java.util.Scanner;

public class MessageProtocol implements MessagingProtocol<String> {

    private boolean shouldTerminate = false;
    private final Database db = Database.getInstance();
    private String user;

    @Override
    public String process(String msg) {
        if (msg.length() == 0) {
            return "ERROR the message is empty!";
        } else {
            /* BECAUSE Short.parseShort() doesnt wanna work for some reason */
            String opcode = msg.substring(0,2);
            Short OPCODE = Short.parseShort(opcode,10);
            if (user == null && OPCODE != 1 && OPCODE != 2 && OPCODE != 3) // the user didnt register yet
                    return err(OPCODE) + "user did not register yet";
//            short OPCODE = bytesToShort(msg.substring(0,2).getBytes(StandardCharsets.UTF_8));
            String[] message = msg.substring(2).split("\0"); // splitting the message
            switch (OPCODE) { // checks which type of message received and will proceed accordingly
                case 1: //ADMINREG
                    return adminReg(message, OPCODE);
                case 2: //STUDENTREG
                    return studentReg(message, OPCODE);
                case 3: //LOGIN
                    return login(message, OPCODE);
                case 4: //LOGOUT
                    return logout(OPCODE);
                case 5: //COURSEREG
                    return courseReg(message, OPCODE);
                case 6: //KDAMCHECK
                    return kdamCheck(message, OPCODE);
                case 7: //COURSESTAT
                    return courseStat(message, OPCODE);
                case 8: //STUDENTSTAT
                    return studentStat(message,OPCODE);
                case 9: //ISREGISTERED
                    return isRegistered(message,OPCODE);
                case 10: //UNREGISTER
                    return unregister(message,OPCODE);
                case 11: //MYCOURSES
                    return myCourses(OPCODE);
                default:
                    return "ERROR NO SUCH COMMAND EXISTS!";
            }
        }
    }

    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    }

    /* ---------- OPCODES ------------ */

    private String adminReg(String[] message, short OPCODE) { //opcode 1
        if (user != null && db.loggedin(user)){
            return err(OPCODE) + "the user is already logged in so cant register";
        }
        try {
            db.adminReg(message[0] /* user */, message[1] /* pass */);
        } catch (Exception e) {
            return err(OPCODE) + e.getMessage();
        }
        this.user = message[0]; // the username
        return ack(OPCODE);
    }

    private String studentReg(String[] message, short OPCODE) { //opcode 2
        if (user != null && db.loggedin(user)){
            return err(OPCODE) + "the user is already logged in so cant register";
        }
        try {
            db.studentReg(message[0] /* user */, message[1] /* pass */);
        } catch (Exception e) {
            return err(OPCODE) + e.getMessage();
        }
        this.user = message[0]; // the username
        return ack(OPCODE);
    }

    private String login(String[] message, short OPCODE) { //opcode 3
        try {
            db.login(message[0], message[1]);
        } catch (Exception e) {
            return err(OPCODE) + e.getMessage();
        } // the logging in was successful
        this.user = message[0]; // initializing the username to be the one who is logged in
        this.shouldTerminate = false;
        return ack(OPCODE);
    }

    private String logout(short OPCODE) { //opcode 4
        try {
            db.logout(user);
        } catch (Exception e) {
            return err(OPCODE) + e.getMessage();
        } // the logout in was successful
        this.user = null;
        this.shouldTerminate = true;
        return ack(OPCODE);
    }

    private String courseReg(String[] message, short OPCODE) { //opcode 5
        try {
            db.courseReg(user, Integer.valueOf(message[0]));
        } catch (Exception e) {
            return err(OPCODE) + e.getMessage();
        } // the courseReg in was successful
        return ack(OPCODE);
    }

    private String kdamCheck(String[] message, short OPCODE) { //opcode 6
        String ans;
        try {
            ans = db.kdamCheck(user, Integer.valueOf(message[0])).toString();
        } catch (Exception e) {
            return err(OPCODE) + e.getMessage();
        } // the kdamCheck in was successful
        return ack(OPCODE) + ans;
    }

    private String courseStat(String[] message, short OPCODE) { //opcode 7
        String ans;
        try {
            ans = db.courseStat(user, Integer.valueOf(message[0]));
        } catch (Exception e) {
            return err(OPCODE) + e.getMessage();
        } // the courseStat in was successful
        return ack(OPCODE) + ans;
    }

    private String studentStat(String[] message, short OPCODE) { //opcode 8 -- this is an ADMIN command
        String ans;
        try {
            ans = db.studentStat(user, (message[0]));
        } catch (Exception e) {
            return err(OPCODE) + e.getMessage();
        } // the courseStat in was successful
        return ack(OPCODE) + ans;
    }

    private String isRegistered(String[] message, short OPCODE) { //opcode 9
        String ans;
        try {
            ans = db.isRegistered(user, Integer.valueOf(message[0]));
        } catch (Exception e) {
            return err(OPCODE) + e.getMessage();
        } // the isRegistered in was successful
        return ack(OPCODE) + ans;
    }

    private String unregister(String[] message, short OPCODE) { //opcode 10
        try {
            db.unregister(user, Integer.valueOf(message[0]));
        } catch (Exception e) {
            return err(OPCODE) + e.getMessage();
        } // the unregister in was successful
        return ack(OPCODE);
    }

    private String myCourses(short OPCODE) { //opcode 11
        String ans;
        try {
            ans = db.myCourses(user);
        } catch (Exception e) {
            return err(OPCODE) + e.getMessage();
        } // the unregister in was successful
        return ack(OPCODE) + ans;
    }

    private String ack(short OPCODE) { //opcode 12
        short ACKOpcode = 12;
        byte[] op;
        op = shortToBytes(OPCODE);
        if (OPCODE < 10)
            return ACKOpcode + "0" + op[1] + "ACK " + OPCODE;
        return ACKOpcode + Byte.toString(op[1]) + "ACK " + OPCODE;
    }

    private String err(short OPCODE) { //opcode 13
        short errorOpcode = 13;
        byte[] op;
        op = shortToBytes(OPCODE);
        if (OPCODE < 10)
            return errorOpcode + "0" + op[1] + "ERROR ";
        return errorOpcode + Byte.toString(op[1]) + "ERROR ";
    }

    public byte[] shortToBytes(short num) {
        byte[] bytesArr = new byte[2];
        bytesArr[0] = (byte) ((num >> 8) & 0xFF);
        bytesArr[1] = (byte) (num & 0xFF);
        return bytesArr;
    }

    private short bytesToShort(byte[] byteArr) {
        short result = (short) ((byteArr[0] & 0xff) << 8);
        result += (short) (byteArr[1] & 0xff);
        return result;
    }
}
