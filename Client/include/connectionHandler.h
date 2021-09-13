#ifndef CONNECTION_HANDLER__
#define CONNECTION_HANDLER__

#include <string>
#include <iostream>
#include <boost/asio.hpp>
#include <vector>
#include "messageEncoderDecoder.h"

using namespace std;


class ConnectionHandler {
private:
    const string host_;
    const short port_;
    boost::asio::io_service io_service_;   // Provides core I/O functionality
    tcp::socket socket_;

public:
    int opcode; //saves the opcode of the decoded message
    messageEncoderDecoder encdec;
    ConnectionHandler(string host, short port);
    virtual ~ConnectionHandler();


    // Connect to the remote machine
    bool connect();

    // Read a fixed number of bytes from the server - blocking.
    // Returns false in case the connection is closed before bytesToRead bytes can be read.
    bool getBytes(char bytes[], unsigned int bytesToRead);

    // Send a fixed number of bytes from the client - blocking.
    // Returns false in case the connection is closed before all the data is sent.
    bool sendBytes(const char bytes[], int bytesToWrite);

    // Read an ascii line from the server
    // Returns false in case connection closed before a newline can be read.
    bool getLine(string& line);

    // Send an ascii line from the server
    // Returns false in case connection closed before all the data is sent.
    bool sendLine(string& line);

    // Get Ascii data from the server until the delimiter character
    // Returns false in case connection closed before null can be read.
    bool getFrameAscii(string& frame, char delimiter);

    // Send a message to the remote host.
    // Returns false in case connection is closed before all the data is sent.
    bool sendFrameAscii(const string& frame, char delimiter);

    // Close down the connection properly.
    void close();

    //gets an empty array and encode the message into it
    int encode(string msg, char *array);

    //read from server, put the answer in the array and prints it
    string decode(char *bytes);

    //decode calls it to identify the server's answer by opcode
    string serverAnswer(int num, char *bytes);

    //read from server the first two bytes and return the opcode of the message
    int checkOpcode(char *arr);

    //copy constructor
    ConnectionHandler(ConnectionHandler &handler);

}; //class ConnectionHandler

#endif