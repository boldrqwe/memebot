package ru.boldr.memebot;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.apache.commons.io.FileUtils;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.CachedDataSet;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.csv.CsvDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;
import org.junit.jupiter.api.extension.TestWatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.boldr.memebot.annotation.DbUnitDataSet;

@Component
public class CsvDatabaseOperationLookup implements TestWatcher, TestExecutionExceptionHandler {

    private DataSource dataSource;
    private IDatabaseConnection connection;

    public CsvDatabaseOperationLookup() {
    }

    @PostConstruct
    public void init() {
        try {
            if (dataSource == null) {
                throw new RuntimeException("dataSource is null");
            }
            connection = new DatabaseConnection(dataSource.getConnection());
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize IDatabaseConnection", e);
        } catch (DatabaseUnitException e) {
            throw new RuntimeException(e);
        }
    }

    public CsvDatabaseOperationLookup(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void testSuccessful(ExtensionContext context) {
        try {
            executeOperation(context, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        try {
            executeOperation(context, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleTestExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
        executeOperation(context, false);
        throw throwable;
    }

    private void executeOperation(ExtensionContext context, boolean isBefore) throws Exception {
        Optional<Method> testMethod = context.getTestMethod();
        if (testMethod.isPresent() && testMethod.get().isAnnotationPresent(DbUnitDataSet.class)) {
            DbUnitDataSet dnUnitDataSet = testMethod.get().getAnnotation(DbUnitDataSet.class);
            String[] files = isBefore ? dnUnitDataSet.before() : dnUnitDataSet.after();
            DatabaseOperation operation = isBefore ? DatabaseOperation.CLEAN_INSERT : DatabaseOperation.DELETE_ALL;

            for (String file : files) {
                IDataSet dataSet = loadDataSet(file);
                operation.execute(connection, dataSet);
            }
        }
    }

    private IDataSet loadDataSet(String fileName) {
        File csvFile = FileUtils.getFile(fileName);

        // Создаем временный каталог
        File tempDirectory;
        try {
            tempDirectory = Files.createTempDirectory("dbunit").toFile();
            tempDirectory.deleteOnExit();
        } catch (IOException e) {
            throw new RuntimeException("Не удалось создать временный каталог", e);
        }

        // Копируем указанный CSV-файл во временный каталог
        try {
            FileUtils.copyFileToDirectory(csvFile, tempDirectory);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось скопировать CSV-файл во временный каталог", e);
        }

        // Создаем файл table-ordering.txt во временном каталоге
        try {
            File tableOrderingFile = new File(tempDirectory, "table-ordering.txt");
            FileWriter writer = new FileWriter(tableOrderingFile);
            writer.write("");
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException("Не удалось создать файл table-ordering.txt", e);
        }

        // Используем временный каталог при создании экземпляра CsvDataSet
        try {
            IDataSet csvDataSet = new CsvDataSet(tempDirectory);
            return new CachedDataSet(csvDataSet);
        } catch (DataSetException e) {
            throw new RuntimeException("Не удалось создать CsvDataSet", e);
        } finally {
            // Удаляем временный каталог
            try {
                FileUtils.deleteDirectory(tempDirectory);
            } catch (IOException e) {
                // Возможно, вы захотите здесь использовать логирование, чтобы избежать проблем с удалением временного
                //каталога
                System.err.println("Не удалось удалить временный каталог: " + tempDirectory.getAbsolutePath());
                e.printStackTrace();
            }
        }
    }
}
