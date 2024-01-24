package com.hysteryale.service;

import com.hysteryale.rollbar.RollbarInitializer;
import com.hysteryale.utils.EnvironmentUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import io.sentry.Sentry;


@Slf4j
public class BasedService extends RollbarInitializer {


    /**
     * To log info to console and rollbar
     *
     * @param message
     * @param exception
     */
    protected void logInfo(String message, Exception... exception) {
        log.info(message, exception);
        //rollbar.info(message);
        logWithSentry(message, exception);
    }

    protected void logDebug(String message, Exception... exception) {
        log.debug(message, exception);
       // rollbar.debug(message);
        logWithSentry(message, exception);
    }

    protected void logError(String message, Exception... exception) {
        log.error(message, exception);
       // rollbar.error(message);
        logWithSentry(message, exception);
    }

    protected void logWarning(String message, Exception... exception) {
        log.warn(message, exception);
//rollbar.warning(message);
        logWithSentry(message, exception);
    }

    private void logWithSentry(String message, Exception... exception){
        Sentry.captureMessage(message);
        if(exception!=null && exception.length > 0) {
            Sentry.captureException(exception[0]);
        }
    }

    protected void updateStateImportFile(String pathFile) {
        //Get path of file imported-file.log
        String pathLogFile = EnvironmentUtils.getEnvironmentValue("import-files.imported-files");
        try {
            File file = new File(pathLogFile);
            BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));
            bw.write(pathFile);
            bw.newLine();
            bw.close();
        } catch (Exception e) {
            logError(e.getMessage());
        }
        logInfo("End importing file: '" + pathFile + "'");
    }


    public boolean isImported(String pathFile) {
        //Get path of file imported-file.log
        String pathLogFile = EnvironmentUtils.getEnvironmentValue("import-files.imported-files");
        try {
            BufferedReader reader = new BufferedReader(new FileReader(pathLogFile));
            String line = reader.readLine();

            while (line != null) {
                if (line.equals(pathFile))
                    return true;
                // read next line
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            logError(e.getMessage());
        }
        return false;
    }

}
