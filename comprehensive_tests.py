import socket
import threading
import time
import os
import random
import string
import json
from datetime import datetime

# Configuration
SERVER_HOST = "localhost"
SERVER_PORT = 8080
TEST_USERNAME = "admin"
TEST_PASSWORD = "admin"
TEST_FILES_DIR = "test_files"
MAX_SLOTS = 10
TEST_REPORT_FILE = "test_report.json"

class TestResult:
    def __init__(self, name, status, message, duration=None):
        self.name = name
        self.status = status
        self.message = message
        self.duration = duration
        self.timestamp = datetime.now().isoformat()

    def to_dict(self):
        return {
            "name": self.name,
            "status": self.status,
            "message": self.message,
            "duration": self.duration,
            "timestamp": self.timestamp
        }

class TestSuite:
    def __init__(self):
        self.results = []
        self.start_time = None
        self.end_time = None

    def start(self):
        self.start_time = time.time()
        print("\n=== Starting Comprehensive Tests ===")
        print(f"Time: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")

    def end(self):
        self.end_time = time.time()
        duration = self.end_time - self.start_time
        print(f"\n=== Tests Completed in {duration:.2f} seconds ===")
        self.save_report()

    def add_result(self, result):
        self.results.append(result)
        status_symbol = "✅" if result.status == "PASS" else "❌"
        print(f"{status_symbol} {result.name}: {result.message}")

    def save_report(self):
        report = {
            "summary": {
                "total_tests": len(self.results),
                "passed_tests": sum(1 for r in self.results if r.status == "PASS"),
                "failed_tests": sum(1 for r in self.results if r.status == "FAIL"),
                "duration": self.end_time - self.start_time,
                "timestamp": datetime.now().isoformat()
            },
            "results": [r.to_dict() for r in self.results]
        }
        
        with open(TEST_REPORT_FILE, 'w') as f:
            json.dump(report, f, indent=2)

def create_test_file(filename, size=1024):
    """Creates a test file with random content."""
    if not os.path.exists(TEST_FILES_DIR):
        os.makedirs(TEST_FILES_DIR)
    
    filepath = os.path.join(TEST_FILES_DIR, filename)
    content = ''.join(random.choices(string.ascii_letters + string.digits, k=size))
    with open(filepath, 'w') as f:
        f.write(content)
    return filepath

def read_file(filepath):
    """Reads file content."""
    with open(filepath, 'r') as f:
        return f.read()

def send_command(sock, command):
    """Sends command to server and returns response."""
    sock.sendall(f"{command}\n".encode())
    return sock.recv(4096).decode().strip()

def test_server_features(suite):
    """Test all server features."""
    print("\n=== Testing Server Features ===")
    
    # Test TCP Server
    try:
        sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        sock.connect((SERVER_HOST, SERVER_PORT))
        suite.add_result(TestResult(
            "TCP Server Connection",
            "PASS",
            "Successfully connected to server"
        ))
    except Exception as e:
        suite.add_result(TestResult(
            "TCP Server Connection",
            "FAIL",
            f"Failed to connect: {str(e)}"
        ))
        return None

    # Test Authentication
    response = send_command(sock, f"AUTH|{TEST_USERNAME}|{TEST_PASSWORD}")
    suite.add_result(TestResult(
        "User Authentication",
        "PASS" if response == "AUTH_OK" else "FAIL",
        f"Authentication response: {response}"
    ))

    # Test File Slot Management
    for slot in range(MAX_SLOTS):
        filename = f"test_file_{slot}.txt"
        filepath = create_test_file(filename)
        content = read_file(filepath)
        response = send_command(sock, f"UPLOAD|{slot}|{filename}|{content}")
        suite.add_result(TestResult(
            f"File Slot {slot} Upload",
            "PASS" if response.startswith("OK") else "FAIL",
            f"Upload response: {response}"
        ))

    # Test File Listing
    response = send_command(sock, "LIST")
    suite.add_result(TestResult(
        "File Listing",
        "PASS" if response else "FAIL",
        f"List response: {response}"
    ))

    # Test File Download
    for slot in range(MAX_SLOTS):
        response = send_command(sock, f"DOWNLOAD|{slot}")
        suite.add_result(TestResult(
            f"File Slot {slot} Download",
            "PASS" if response.startswith("OK|") else "FAIL",
            f"Download response: {response[:100]}..."
        ))

    # Test File Deletion
    for slot in range(MAX_SLOTS):
        response = send_command(sock, f"DELETE|{slot}")
        suite.add_result(TestResult(
            f"File Slot {slot} Deletion",
            "PASS" if response.startswith("OK") else "FAIL",
            f"Delete response: {response}"
        ))

    return sock

def test_protocol_implementation(suite, sock):
    """Test protocol implementation."""
    print("\n=== Testing Protocol Implementation ===")
    
    # Test AUTH command
    response = send_command(sock, f"AUTH|{TEST_USERNAME}|{TEST_PASSWORD}")
    suite.add_result(TestResult(
        "AUTH Command",
        "PASS" if response == "AUTH_OK" else "FAIL",
        f"AUTH response: {response}"
    ))

    # Test LIST command
    response = send_command(sock, "LIST")
    suite.add_result(TestResult(
        "LIST Command",
        "PASS" if response else "FAIL",
        f"LIST response: {response}"
    ))

    # Test UPLOAD command
    filename = "protocol_test.txt"
    filepath = create_test_file(filename)
    content = read_file(filepath)
    response = send_command(sock, f"UPLOAD|0|{filename}|{content}")
    suite.add_result(TestResult(
        "UPLOAD Command",
        "PASS" if response.startswith("OK") else "FAIL",
        f"UPLOAD response: {response}"
    ))

    # Test DOWNLOAD command
    response = send_command(sock, "DOWNLOAD|0")
    suite.add_result(TestResult(
        "DOWNLOAD Command",
        "PASS" if response.startswith("OK|") else "FAIL",
        f"DOWNLOAD response: {response[:100]}..."
    ))

    # Test DELETE command
    response = send_command(sock, "DELETE|0")
    suite.add_result(TestResult(
        "DELETE Command",
        "PASS" if response.startswith("OK") else "FAIL",
        f"DELETE response: {response}"
    ))

def test_concurrent_access(suite):
    """Test concurrent access to server."""
    print("\n=== Testing Concurrent Access ===")
    
    results = []
    def client_thread():
        try:
            sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            sock.connect((SERVER_HOST, SERVER_PORT))
            response = send_command(sock, f"AUTH|{TEST_USERNAME}|{TEST_PASSWORD}")
            results.append(response == "AUTH_OK")
            sock.close()
        except Exception:
            results.append(False)
    
    threads = []
    for i in range(5):
        t = threading.Thread(target=client_thread)
        threads.append(t)
        t.start()
    
    for t in threads:
        t.join()
    
    success_count = sum(1 for r in results if r)
    suite.add_result(TestResult(
        "Concurrent Access",
        "PASS" if success_count == 5 else "FAIL",
        f"Successful connections: {success_count}/5"
    ))

def test_error_handling(suite, sock):
    """Test error handling."""
    print("\n=== Testing Error Handling ===")
    
    # Test invalid slot
    response = send_command(sock, "DOWNLOAD|999")
    suite.add_result(TestResult(
        "Invalid Slot Download",
        "PASS" if response.startswith("ERROR") else "FAIL",
        f"Response: {response}"
    ))

    # Test invalid authentication
    response = send_command(sock, "AUTH|invalid|invalid")
    suite.add_result(TestResult(
        "Invalid Authentication",
        "PASS" if response == "AUTH_FAIL" else "FAIL",
        f"Response: {response}"
    ))

    # Test invalid command
    response = send_command(sock, "INVALID_COMMAND")
    suite.add_result(TestResult(
        "Invalid Command",
        "PASS" if response.startswith("ERROR") else "FAIL",
        f"Response: {response}"
    ))

def cleanup():
    """Clean up test files."""
    if os.path.exists(TEST_FILES_DIR):
        for file in os.listdir(TEST_FILES_DIR):
            os.remove(os.path.join(TEST_FILES_DIR, file))
        os.rmdir(TEST_FILES_DIR)

def main():
    suite = TestSuite()
    suite.start()
    
    # Run all tests
    sock = test_server_features(suite)
    if sock:
        test_protocol_implementation(suite, sock)
        test_error_handling(suite, sock)
        sock.close()
    
    test_concurrent_access(suite)
    
    # Cleanup
    cleanup()
    
    # End test suite
    suite.end()

if __name__ == "__main__":
    main() 