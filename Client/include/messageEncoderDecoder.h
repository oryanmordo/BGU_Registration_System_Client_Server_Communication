//
// Created by spl211 on 26/12/2020.
//

#ifndef ASSIGNMENT1_ZIP_MESSAGEENCODERDECODER_H
#define ASSIGNMENT1_ZIP_MESSAGEENCODERDECODER_H

#endif //ASSIGNMENT1_ZIP_MESSAGEENCODERDECODER_H


#include <string>
#include <list>
#include <vector>
#include <iostream>
#include <boost/algorithm/string.hpp>
#include <stdio.h>
#include <string.h>
#include <boost/asio.hpp>

using boost::asio::ip::tcp;

using namespace std;

enum class byte : std::uint8_t {};

class messageEncoderDecoder {
private:
    vector<byte> bytes;
    short opcode = 0;  //saves the opcode of the encoded message
    string action = "";  //saves the first word the keyboard gets
public:
    messageEncoderDecoder();
    int encode(string message, char *array);  //gets an empty array and encode the message into it
    string decodeNextByte(byte nextByte);  //not in use
    int getOpcode();  //return opcode
};