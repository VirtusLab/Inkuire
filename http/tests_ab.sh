#!/bin/bash
ab -n 10 -c 4 http://localhost:6969/forSignature\?signature\=Seq%5BInt%5D%20%3D%3E%20%28Int%20%3D%3E%20Long%29%20%3D%3E%20Seq%5BLong%5D
ab -n 10 -c 4 http://localhost:6969/forSignature\?signature\=Set%5BLong%5D%20%3D%3E%20Long%20%3D%3E%20Boolean
ab -n 10 -c 4 http://localhost:6969/forSignature\?signature\=List%5B_%5D%3D%3ESet%5B_%5D