package bgu.spl.net.impl.echo;

import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.srv.ConnectionHandler;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Scanner;


public class LineMessageEncoderDecoder implements MessageEncoderDecoder<String> {

    private byte[] bytes = new byte[1 << 10]; //start with 1k
    private int len = 0;
    private short OPCODE = 0;
    private int zeroCounter = 0;
    private int byteCounter = 0;

    @Override
    public String decodeNextByte(byte nextByte) {
        //notice that the top 128 ascii characters have the same representation as their utf-8 counterparts
        //this allow us to do the following comparison
        if (this.len < 2) {// waiting to get the opcode
            pushByte(nextByte);
            if (this.len == 2) { // logout or mycourses
                byte[] aa = new byte[2];
                aa[0] = this.bytes[0];
                aa[1] = this.bytes[1];
                getOPCODE(aa);
                if (this.OPCODE == 4 || this.OPCODE == 11)
                    return decodeMessage(nextByte);
            }
            return null; //not a line yet
        } else { // len == 2 -- we got the opcode
            String msg = decodeMessage(nextByte);
            return msg;
        }
    }


    public static void main(String[] args) {
        LineMessageEncoderDecoder a = new LineMessageEncoderDecoder();
        byte[] aa = new byte[2];
        aa[0] = 48;
        aa[1] = 49;

        short opcode = a.getOPCODE(aa);
        System.out.printf(String.valueOf(opcode));
    }

    private short getOPCODE(byte[] byteArr) {
        String opString = new String(byteArr, StandardCharsets.UTF_8);
        Short OP = Short.parseShort(opString, 10);
        this.OPCODE = OP;
        return OP;
    }

    private String decodeMessage(byte nextByte) {
        if (this.OPCODE == 0) { // because this method is recursive it needs to know if we already have the current message opcode
            /* BECAUSE Short.parseShort() doesnt wanna work for some reason */
            byte[] aa = new byte[2];
            aa[0] = this.bytes[0];
            aa[1] = this.bytes[1];
            getOPCODE(aa);
        }
        switch (OPCODE) {
            case 1: // ADMINREG
            case 2: // STUDENTREG
            case 3: // LOGIN
                if (nextByte == '\0')
                    zeroCounter++;
                pushByte(nextByte);
                if (zeroCounter == 2) // user 0 password 0
                    return popString();
                return null;
            case 4: // LOGOUT
            case 11: // MYCOURSES
                return popString();
            case 5: // COURSEREG
            case 10: // UNREGISTER
            case 9: // ISREGISTERED
            case 7: // COURSESTAT
            case 6: // KDAMCHECK
                /* in this case ill just get another two bytes (the course number) */
                pushByte(nextByte);
                byteCounter++;
                if (byteCounter == 2)
                    return popString();
                return null;
            case 8: // STUDENTSTAT
                /* in this case we need to get another \0 for the end of the message*/
                if (nextByte == '\0')
                    zeroCounter++;
                pushByte(nextByte);
                if (zeroCounter == 1) // user 0 password 0
                    return popString();
                return null;
            default:
                throw new IllegalArgumentException("Illegal OPCODE (not 1-11)");
        }
    }

    @Override
    public byte[] encode(String message) {
        return (message + "\0").getBytes(); //uses utf8 by default
    }

    private void pushByte(byte nextByte) {
        if (len >= bytes.length) {
            bytes = Arrays.copyOf(bytes, len * 2);
        }
        bytes[len++] = nextByte;
    }

    private String popString() {
        //notice that we explicitly requesting that the string will be decoded from UTF-8
        //this is not actually required as it is the default encoding in java.
        String result = new String(bytes, 0, len, StandardCharsets.UTF_8);
        prepForNewMsg();
        return result;
    }

    private void prepForNewMsg() {
        this.len = 0;
        this.OPCODE = 0;
        this.zeroCounter = 0;
        this.byteCounter = 0;
    }

    public short bytesToShort(byte[] byteArr) {
        short result = (short) ((byteArr[0] & 0xff) << 8);
        result += (short) (byteArr[1] & 0xff);
        return result;
    }
}
