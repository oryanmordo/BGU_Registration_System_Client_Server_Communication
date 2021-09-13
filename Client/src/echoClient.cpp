//#include <stdlib.h>
//#include <connectionHandler.h>
//


/**
* This code assumes that the server replies the exact text the client sent it (as opposed to the practical session example)
*/
//int main (int argc, char *argv[]) {
//    if (argc < 3) {
//        cerr << "Usage: " << argv[0] << " host port" << std::endl << std::endl;
//        return -1;
//    }
//    string host = argv[1];
//    short port = atoi(argv[2]);
//
//    ConnectionHandler connectionHandler(host, port);
//    if (!connectionHandler.connect()) {
//        cerr << "Cannot connect to " << host << ":" << port << endl;
//        return 1;
//    }
//
//    //From here we will see the rest of the ehco client implementation:
//    while (1) {
//        const short bufsize = 1024;
//        char buf[bufsize];
//        cin.getline(buf, bufsize);
//        string line(buf);
//        int len=line.length();
//        if (!connectionHandler.sendLine(line)) {
//            cout << "Disconnected. Exiting...\n" << endl;
//            break;
//        }
//        // connectionHandler.sendLine(line) appends '\n' to the message. Therefor we send len+1 bytes.
//        cout << "Sent " << len+1 << " bytes to server" << endl;
//
//
//        // We can use one of three options to read data from the server:
//        // 1. Read a fixed number of characters
//        // 2. Read a line (up to the newline character using the getline() buffered reader
//        // 3. Read up to the null character
//        string answer;
//        // Get back an answer: by using the expected number of bytes (len bytes + newline delimiter)
//        // We could also use: connectionHandler.getline(answer) and then get the answer without the newline char at the end
//        if (!connectionHandler.getLine(answer)) {
//            cout << "Disconnected. Exiting...\n" << endl;
//            break;
//        }
//
//        len=answer.length();
//        // A C string must end with a 0 char delimiter.  When we filled the answer buffer from the socket
//        // we filled up to the \n char - we must make sure now that a 0 char is also present. So we truncate last character.
//        answer.resize(len-1);
//        cout << "Reply: " << answer << " " << len << " bytes " << endl << endl;
//        if (answer == "bye") {
//            cout << "Exiting...\n" << endl;
//            break;
//        }
//    }
//    return 0;
//}