import socket
import threading
import time
import os
import random
import string

# Configuration
SERVER_HOST = "localhost"
SERVER_PORT = 8080
TEST_USERNAME = "admin"
TEST_PASSWORD = "admin"
TEST_FILES_DIR = "test_files"

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

def test_connection():
    """Test basic server connection."""
    print("\nTesting server connection...")
    try:
        sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        sock.connect((SERVER_HOST, SERVER_PORT))
        print("✅ Server connection successful")
        return sock
    except Exception as e:
        print(f"❌ Server connection error: {e}")
        return None

def test_authentication(sock):
    """Test authentication."""
    print("\nTesting authentication...")
    try:
        response = send_command(sock, f"AUTH|{TEST_USERNAME}|{TEST_PASSWORD}")
        if response == "AUTH_OK":
            print("✅ Authentication successful")
            return True
        else:
            print(f"❌ Authentication failed: {response}")
            return False
    except Exception as e:
        print(f"❌ Authentication error: {e}")
        return False

def test_list_files(sock):
    """Test file listing."""
    print("\nTesting file listing...")
    try:
        response = send_command(sock, "LIST")
        print(f"✅ File listing: {response}")
        return True
    except Exception as e:
        print(f"❌ File listing error: {e}")
        return False

def test_upload_file(sock, slot, filename):
    """Test file upload."""
    print(f"\nTesting file upload to slot {slot}...")
    try:
        filepath = create_test_file(filename)
        content = read_file(filepath)
        response = send_command(sock, f"UPLOAD|{slot}|{filename}|{content}")
        if response.startswith("OK"):
            print(f"✅ File upload successful: {response}")
            return True
        else:
            print(f"❌ File upload failed: {response}")
            return False
    except Exception as e:
        print(f"❌ File upload error: {e}")
        return False

def test_download_file(sock, slot):
    """Test file download."""
    print(f"\nTesting file download from slot {slot}...")
    try:
        response = send_command(sock, f"DOWNLOAD|{slot}")
        if response.startswith("OK|"):
            print(f"✅ File download successful: {response[:100]}...")
            return True
        else:
            print(f"❌ File download failed: {response}")
            return False
    except Exception as e:
        print(f"❌ File download error: {e}")
        return False

def test_delete_file(sock, slot):
    """Test file deletion."""
    print(f"\nTesting file deletion from slot {slot}...")
    try:
        response = send_command(sock, f"DELETE|{slot}")
        if response.startswith("OK"):
            print(f"✅ File deletion successful: {response}")
            return True
        else:
            print(f"❌ File deletion failed: {response}")
            return False
    except Exception as e:
        print(f"❌ File deletion error: {e}")
        return False

def test_concurrent_connections():
    """Test concurrent connections."""
    print("\nTesting concurrent connections...")
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
    for _ in range(5):  # Test 5 concurrent connections
        t = threading.Thread(target=client_thread)
        threads.append(t)
        t.start()
    
    for t in threads:
        t.join()
    
    success_count = sum(1 for r in results if r)
    print(f"✅ Concurrent connections: {success_count}/5 successful")
    return success_count == 5

def cleanup():
    """Clean up test files."""
    if os.path.exists(TEST_FILES_DIR):
        for file in os.listdir(TEST_FILES_DIR):
            os.remove(os.path.join(TEST_FILES_DIR, file))
        os.rmdir(TEST_FILES_DIR)

def main():
    print("=== Starting Tests ===")
    
    # Basic tests
    sock = test_connection()
    if not sock:
        return
    
    if not test_authentication(sock):
        sock.close()
        return
    
    test_list_files(sock)
    
    # File operation tests
    test_upload_file(sock, "0", "test1.txt")
    test_list_files(sock)
    test_download_file(sock, "0")
    test_delete_file(sock, "0")
    
    # Error state tests
    print("\nTesting error states...")
    test_upload_file(sock, "999", "test2.txt")  # Non-existent slot
    test_download_file(sock, "999")  # Non-existent slot
    test_delete_file(sock, "999")  # Non-existent slot
    
    # Concurrent connection test
    test_concurrent_connections()
    
    sock.close()
    cleanup()
    print("\n=== Tests Completed ===")

if __name__ == "__main__":
    main()