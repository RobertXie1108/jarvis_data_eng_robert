package ca.jrvs.apps.practice;

import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegexExcImp implements RegexExc{
    @Override
    public boolean matchJpeg(String filename) {
        Pattern jpegPattern = Pattern.compile("");
        return jpegPattern.matcher(filename).matches();
    }

    @Override
    public boolean matchIp(String ip) {
        Pattern ipPattern = Pattern.compile("^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$");
        return ipPattern.matcher(ip).matches();
    }

    @Override
    public boolean isEmptyLine(String line) {
        Pattern emptyLinePattern = Pattern.compile("^\\s*$");
        return emptyLinePattern.matcher(line).matches();
    }

    public static void main(String[] args) {
        RegexExcImp regeximp = new RegexExcImp();
        Logger logger = LoggerFactory.getLogger("");
        logger.info("Match JPEG 'somefile.jpg' " + regeximp.matchJpeg("somefile.jpg"));
        logger.info("Match JPEG 'somefile.jpeg' " + regeximp.matchJpeg("somefile.jpeg"));
        logger.info("Match JPEG 'somefile.jpp' " + regeximp.matchJpeg("somefile.jpp"));
        logger.info("Match JPEG '.jpg' " + regeximp.matchJpeg(".jpg"));

        logger.info("Match IP '0.0.0.0' " + regeximp.matchIp("0.0.0.0"));
        logger.info("Match IP '0,0,0,0' " + regeximp.matchIp("0,0,0,0"));
        logger.info("Match IP '255.255.255.255' " + regeximp.matchIp("255.255.255.255"));
        logger.info("Match IP '0.00.000.0000' " + regeximp.matchIp("0.00.000.0000"));
        logger.info("Match IP '192.168.0.F' " + regeximp.matchIp("192.168.0.F"));

        logger.info("Match empty line '' " + regeximp.isEmptyLine(""));
        logger.info("Match empty line 'aaa' " + regeximp.isEmptyLine("aaa"));
        logger.info("Match empty line '\\n' " + regeximp.isEmptyLine("\n"));
    }
}
