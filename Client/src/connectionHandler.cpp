#include "../include/connectionHandler.h"
#include <unistd.h>


using std::cin;
using std::cout;
using std::cerr;
using std::endl;
using std::string;

ConnectionHandler::ConnectionHandler(string host, short port) : host_(host), port_(port), io_service_(),
                                                                socket_(io_service_), opcode(14), encdec() {}

ConnectionHandler::~ConnectionHandler() {
    close();
}


bool ConnectionHandler::connect() {
    cout << "Starting connect to "
         << host_ << ":" << port_ << endl;
    try {
        tcp::endpoint endpoint(boost::asio::ip::address::from_string(host_), port_); // the server endpoint
        boost::system::error_code error;
        socket_.connect(endpoint, error);
        if (error)
            throw boost::system::system_error(error);
    }
    catch (exception &e) {
        cerr << "Connection failed (Error: " << e.what() << ')' << endl;
        return false;
    }
    return true;
}

bool ConnectionHandler::getBytes(char bytes[], unsigned int bytesToRead) {
    size_t tmp = 0;
    boost::system::error_code error;
    try {
        while (!error && bytesToRead > tmp) {
            tmp += socket_.read_some(boost::asio::buffer(bytes + tmp, bytesToRead - tmp), error);
        }
        if (error)
            throw boost::system::system_error(error);
    } catch (std::exception &e) {
        return false;
    }
    return true;
}


bool ConnectionHandler::sendBytes(const char bytes[], int bytesToWrite) {
    int tmp = 0;
    boost::system::error_code error;
    try {
        while (!error && bytesToWrite > tmp) {
            tmp += socket_.write_some(boost::asio::buffer(bytes + tmp, bytesToWrite - tmp), error);
        }
        if (error)
            throw boost::system::system_error(error);
    } catch (std::exception &e) {
        std::cerr << "recv failed (Error: " << e.what() << ')' << std::endl;
        return false;
    }
    return true;
}

bool ConnectionHandler::getLine(std::string &line) {
    return getFrameAscii(line, '\0');
}

bool ConnectionHandler::sendLine(std::string &line) {
    return sendFrameAscii(line, '\0');
}


bool ConnectionHandler::getFrameAscii(std::string &frame, char delimiter) {
    char ch;
    // Stop when we encounter the null character.
    // Notice that the null character is not appended to the frame string.
    try {
        do {
            if (!getBytes(&ch, 1)) {
                return false;
            }
            if (ch != '\0')
                frame.append(1, ch);
        } while (delimiter != ch);
    } catch (std::exception &e) {
        std::cerr << "recv failed2 (Error: " << e.what() << ')' << std::endl;
        return false;
    }
    return true;
}


bool ConnectionHandler::sendFrameAscii(const string &frame, char delimiter) {
    bool result = sendBytes(frame.c_str(), frame.length());
    if (!result) return false;
    return sendBytes(&delimiter, 1);
}

// Close down the connection properly.
void ConnectionHandler::close() {
    try {
        socket_.close();
    } catch (...) {
        std::cout << "closing failed: connection already closed" << std::endl;
    }
}

int ConnectionHandler::encode(string msg, char *array) {
    return encdec.encode(msg, array);
}

string ConnectionHandler::decode(char *bytes) {
    //if (strcmp(bytes, "")) return "";
    opcode = checkOpcode(bytes);
    switch (opcode) {
        case 0:
            return "0";
        case 1:
        case 2:
        case 3:
        case 4:
        case 5:
        case 10:
            //return only ACK + opc
            return serverAnswer(1, bytes);
        case 6: //return list of Kdam Courses
        case 7: //return details of the course
        case 8: //return student status
        case 9: //return REGISTERED/NOT REGISTERED
            return serverAnswer(2, bytes);
        case 11: //return list of current courses
            return serverAnswer(4, bytes);
        case 13:
            return serverAnswer(0, bytes); //ERROR
        case 14: //ERROR for LOGOUT
            return serverAnswer(3, bytes);
        default:
            cout << "got opcode: " << opcode << endl;
            return "";

    }
    // return "";
}

/* prints the servers answer and returns it */
string ConnectionHandler::serverAnswer(int num, char *bytes) {
    string answer;
    int temp = 0;
    do {
        getBytes(bytes, 1);
        answer = answer + bytes[0];
        temp++;
    } while (bytes[0] != '\0');
    if (num == 0) { //ERROR
        cout << answer.substr(0, answer.size() - 1) << endl;
    } else if (num == 1) { //return just ACK + opc
        cout << answer.substr(0, answer.size() - 1) << endl;
    } else if (num == 2) { //return ACK + opc + answer
        cout << answer.substr(0, 5) << endl;
        cout << answer.substr(5, answer.size() - 1) << endl;
    } else if (num == 3) { //ERROR for LOGOUT
        cout << answer.substr(0, answer.size() - 1) << endl;
        return "4" + answer;
    } else if (num == 4) { //return ACK + opc + answer for opcode 11
        cout << answer.substr(0, 6) << endl;
        cout << answer.substr(6, answer.size() - 1) << endl;
    }
    return answer;
}

int ConnectionHandler::checkOpcode(char *arr) {
    getBytes(arr, 4);
    if (strcmp(arr,"") == 0) return 0;
    if (arr[0] == '1' && arr[1] == '2') { //ACK
        if (arr[2] == '0') { //opcode between 1-9
            if (arr[3] == '1') return 1;
            if (arr[3] == '2') return 2;
            if (arr[3] == '3') return 3;
            if (arr[3] == '4') return 4;
            if (arr[3] == '5') return 5;
            if (arr[3] == '6') return 6;
            if (arr[3] == '7') return 7;
            if (arr[3] == '8') return 8;
            if (arr[3] == '9') return 9;
        }
        if (arr[2] == '1') {
            if (arr[3] == '0') return 10;
            if (arr[3] == '1') return 11;
        }
    }
    if (arr[0] == '1' && arr[1] == '3') { //ERROR
        if (arr[2] == '0' && arr[3] == '4') return 14; //ERROR FOR LOGOUT
        else return 13;
    }
    return 0;
}

ConnectionHandler::ConnectionHandler(ConnectionHandler &handler) : host_(handler.host_), port_(handler.port_),
                                                                   io_service_(), socket_(handler.io_service_), opcode(handler.opcode), encdec(handler.encdec) {}








