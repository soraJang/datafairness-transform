package com.mobigen.iris.jdbc;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.sql.SQLException;


public class IRISSocket {
    private Byte remain_buf = null;
    Socket rawSock;
    BufferedInputStream bis;
    BufferedOutputStream bos;
    boolean isClose = true;

    IRISSocket(Socket sock) throws SQLException {
        this.rawSock = sock;

        try {
            this.bis = new BufferedInputStream(sock.getInputStream());
            this.bos = new BufferedOutputStream(sock.getOutputStream());
        } catch (IOException e) {
            this.bis = null;
            this.bos = null;
            throw new SQLException(e);
        }
        remain_buf = new Byte();
        isClose = false;
    }

    public void close() {
        isClose = true;
        this.rawSock = null;
        try {
            bis.close();
            bis = null;
        } catch (Exception e) {
        }
        try {
            bos.close();
            bos = null;
        } catch (Exception e) {
        }
    }

    public boolean write(String sendMsg) throws SQLException {
        if (this.isClose) {
            throw new SQLException("socket is closed");
        }

        try {
//			System.out.println("SEND " + this.rawSock.getSoTimeout() + " : " + sendMsg.trim());
            this.bos.write(sendMsg.getBytes("UTF-8"));
            this.bos.flush();
        } catch (Exception e) {
            this.isClose = true;
            return false;
        }

        return true;
    }

    public boolean write(byte[] sendMsg) throws SQLException {
        if (this.isClose) {
            throw new SQLException("socket is closed");
        }

        try {
            this.bos.write(sendMsg);
            this.bos.flush();
        } catch (IOException e) {
            return false;
        }

        return true;
    }

    public boolean write(byte[] sendMsg, int len) throws SQLException {
        if (this.isClose) {
            throw new SQLException("socket is closed");
        }

        try {
            this.bos.write(sendMsg, 0, len);
            this.bos.flush();
        } catch (IOException e) {
            return false;
        }

        return true;
    }

    public String readline() throws SQLException {
        if (this.isClose) {
            throw new SQLException("socket is closed");
        }

        //System.out.println("readline() =================================");
        int len, offset;
        byte[] buf = new byte[Default.BUFFER_SIZE];
        Byte response = new Byte();

        if ((len = remain_buf.length()) != 0) {
            //System.out.println("remain_buf !");

            if ((offset = remain_buf.indexOf((byte) '\n')) == -1) {
                //System.out.println("remain_buf (no new line)");
                response.set(remain_buf.get());
                remain_buf.none();
                //System.out.println("response : "+new String(response.get(), "utf8").trim());
                //System.out.println("remain_buf : "+new String(remain_buf.get(), "utf8").trim());

            } else {
                //System.out.println("remain_buf (new line)");
                response.add(remain_buf.get(0, offset + 1));
                remain_buf.set(remain_buf.get(offset + 1, -1));
                //System.out.println("response : "+new String(response.get(), "utf8").trim());
                //System.out.println("remain_buf : "+new String(remain_buf.get(), "utf8").trim());

                try {
                    return new String(response.get(), "utf8").trim();
                } catch (UnsupportedEncodingException e) {
                    throw new SQLException(e);
                }
            }
        }

        try {
            while ((len = this.bis.read(buf, 0, Default.BUFFER_SIZE)) > 0) {
                //System.out.println("buffer !");
//				System.out.println(">>>>>>>>");
//				System.out.println("buf "+ this.rawSock.getSoTimeout() + " : " + (new String(buf, 0, len, "utf8")).trim());
//				System.out.println("<<<<<<<<<");
                offset = -1;
                for (int i = 0; i < len; i++) {
                    if (buf[i] == '\n') {
                        offset = i;
                        break;
                    }
                }
                if (offset == -1) {
                    //System.out.println("buffer (no new line)");
                    response.add(buf, 0, len);
                } else {
                    //System.out.println("buffer (new line)");
                    response.add(buf, 0, offset + 1);
                    if (offset + 1 == len) {
                        remain_buf.none();
                    } else {
                        remain_buf.add(buf, offset + 1, len - offset - 1);
                    }
                    break;
                }
            }

            //System.out.println("response : "+new String(response.get(), "utf8").trim());
            //System.out.println("remain_buf : "+new String(remain_buf.get(), "utf8").trim());
            return new String(response.get(), "utf8").trim();
        } catch (SocketTimeoutException e) {
            int socket_time = -1;
            try {
                socket_time = this.rawSock.getSoTimeout();
            } catch (SocketException e1) {
            }
            this.close();
            throw new SQLException(e + " :" + socket_time + "ms");
        } catch (Exception e) {
            this.close();
            throw new SQLException(e);
        }
    }

    public int readByte(byte[] buf, int readSize) throws SQLException {
        if (this.isClose) {
            throw new SQLException("socket is closed");
        }

        try {
            return this.bis.read(buf, 0, readSize);
        } catch (IOException e) {
            throw new SQLException(e);
        }
    }

    public String read(int readSize) throws SQLException {
        if (this.isClose) {
            throw new SQLException("socket is closed");
        }

        //System.out.println("read() =================================");

        int len, count;
        byte[] buf = new byte[Default.BUFFER_SIZE];
        Byte response = new Byte();

        if ((len = remain_buf.length()) != 0) {
            if (len < readSize) {
                //System.out.println("remain_buf (some)");
                response.set(remain_buf.get());
                remain_buf.none();
                //System.out.println("response : "+new String(response.get(), "utf8").trim());
                //System.out.println("remain_buf : "+new String(remain_buf.get(), "utf8").trim());
            } else {
                //System.out.println("remain_buf (all)");
                response.add(remain_buf.get(0, readSize));
                remain_buf.set(remain_buf.get(readSize, -1));
                //System.out.println("response : "+new String(response.get(), "utf8").trim());
                //System.out.println("remain_buf : "+new String(remain_buf.get(), "utf8").trim());
                try {
                    return new String(response.get(), "utf8").trim();
                } catch (UnsupportedEncodingException e) {
                    throw new SQLException(e);
                }
            }
        }

        count = len;

        try {
            while ((len = this.bis.read(buf, 0, Default.BUFFER_SIZE)) > 0) {
                //System.out.println("buffer !");
                //System.out.println("buf :"+new String(buf, 0, len, "utf8"));

                if (count + len == readSize) {
                    //System.out.println("buffer (all)");
                    response.add(buf, 0, len);
                    //System.out.println("response : "+new String(response.get(), "utf8").trim());
                    //System.out.println("remain_buf : "+new String(remain_buf.get(), "utf8").trim());
                    break;
                } else if (count + len > readSize) {
                    //System.out.println("buffer (all +)");
                    response.add(buf, 0, readSize - count);
                    remain_buf.add(buf, readSize - count, -1);
                    //System.out.println("response : "+new String(response.get(), "utf8").trim());
                    //System.out.println("remain_buf : "+new String(remain_buf.get(), "utf8").trim());
                    break;
                }
                //System.out.println("buffer (some)");

                response.add(buf, 0, len);
                count += len;
                //System.out.println("response : "+new String(response.get(), "utf8").trim());
                //System.out.println("remain_buf : "+new String(remain_buf.get(), "utf8").trim());
            }

            return new String(response.get(), "utf8").trim();
        } catch (SocketTimeoutException e) {
            int socket_time = -1;
            try {
                socket_time = this.rawSock.getSoTimeout();
            } catch (SocketException e1) {
            }
            this.close();
            throw new SQLException(e + " :" + socket_time + "ms");
        } catch (Exception e) {
            this.close();
            throw new SQLException(e);
        }
    }
}