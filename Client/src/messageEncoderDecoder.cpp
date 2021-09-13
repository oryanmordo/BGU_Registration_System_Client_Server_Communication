//
// Created by spl211 on 29/12/2020.
//

#include "../include/messageEncoderDecoder.h"

messageEncoderDecoder::messageEncoderDecoder(): bytes() {}

int messageEncoderDecoder::encode(string message, char *array) {
    unsigned int i = 0;
    opcode = 0;
    while (message[i] != ' ' && i < message.length()) {
        action =  action + message[i];
        i++;
    }
    char arr[] = {'0','0'};
    //find opcode and remove action from message:
    if (action == "ADMINREG") {
        opcode = 1;
        message = message.substr(9, message.length()-1);
        message = message + ' ';
    }
    else if (action == "STUDENTREG") {
        opcode = 2;
        message = message.substr(11, message.length()-1);
        message = message + ' ';
    }
    else if (action == "LOGIN") {
        opcode = 3;
        message = message.substr(6, message.length()-1);
        message = message + ' ';
    }
    else if (action == "LOGOUT") {
        opcode = 4;
        message = "";
    }
    else if (action == "COURSEREG") {
        opcode = 5;
        message = message.substr(10, message.length()-1);
        if (message.length() == 1) message = "0" + message; //case course number <10
    }
    else if (action == "KDAMCHECK") {
        opcode = 6;
        message = message.substr(10, message.length()-1);
        if (message.length() == 1) message = "0" + message; //case course number <10
    }
    else if (action == "COURSESTAT") {
        opcode = 7;
        message = message.substr(11, message.length()-1);
        if (message.length() == 1) message = "0" + message; //case course number <10
    }
    else if (action == "STUDENTSTAT") {
        opcode = 8;
        message = message.substr(12, message.length()-1);
        message = message + ' ';
    }
    else if (action == "ISREGISTERED") {
        opcode = 9;
        message = message.substr(13, message.length()-1);
        if (message.length() == 1) message = "0" + message; //case course number <10
    }
    else if (action == "UNREGISTER") {
        opcode = 10;
        message = message.substr(11, message.length()-1);
        if (message.length() == 1) message = "0" + message; //case course number <10
    }
    else if (action == "MYCOURSES") {
        opcode = 11;
        //message = message.substr(10, message.length()-1);
        message = "";
    }

    //put opcode in arr:
    if (opcode < 10) {
        arr[1] = opcode + '0';
    }
    else if (opcode == 10) {
        arr[0] = '1';
    }
    else if (opcode == 11) {
        arr[1] = '1';
        arr[0] = '1';
    }
    action = "";
    char msg1[message.length()];

    //replace all ' ' in '\0':
    for (unsigned int j = 0; j < message.length(); j++) {
        if(message[j] == ' ')
            msg1[j] = '\0';
        else
            msg1[j] = message[j];
    }

    //put opcode before message:
    array[0] = arr[0];
    array[1] = arr[1];
    for (unsigned int j = 2; j < message.length()+2; ++j) {
        array[j] = msg1[j-2];
    }
    return message.length() + 2;
}

string messageEncoderDecoder::decodeNextByte(byte nextByte) {
    return nullptr;
}


int messageEncoderDecoder::getOpcode() { return opcode; }