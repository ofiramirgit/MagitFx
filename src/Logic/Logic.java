package Logic;

import inputValidation.FilesValidation;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Logic {

    FilesValidation m_FilesValidation = new FilesValidation();
    
    public boolean initRepository(File selectedFolder) {
        if(m_FilesValidation.isRepository(selectedFolder))
            return false;
        else
            createRepository(selectedFolder);
        return true;
    }

    private void createRepository(File selectedFolder) {
        String RepoName = selectedFolder.getName();
        String RepoPathString = selectedFolder.toString();
        Path RepositoryPath = Paths.get(RepoPathString + File.separator + RepoName);
        Path RootFolderPath = Paths.get(RepoPathString + File.separator + RepoName + File.separator + RepoName);
        Path ObjectPath = Paths.get(RepoPathString + File.separator + RepoName + File.separator + ".magit" + File.separator + "objects");
        Path branchesPath = Paths.get(RepoPathString + File.separator + RepoName + File.separator + ".magit" + File.separator + "branches");
        Path activeBranchePath = Paths.get(RepoPathString+ File.separator + RepoName + File.separator + ".magit" + File.separator + "branches" + File.separator + "HEAD.txt");
        Path branchesNamesPath = Paths.get(RepoPathString + File.separator + RepoName + File.separator + ".magit" + File.separator + "branches" + File.separator + "NAMES.txt");
        Path rootFolderNamePath = Paths.get(RepoPathString + File.separator + RepoName + File.separator + ".magit" + File.separator + "RootFolderName.txt");
        Path commitStatusPath = Paths.get(RepoPathString + File.separator + RepoName + File.separator + ".magit" + File.separator + "CommitStatus.txt");
        Path materFilePath = Paths.get(RepoPathString + File.separator + RepoName + File.separator + ".magit" + File.separator + "branches" +File.separator +"mater.txt");
        Boolean dirExists = Files.exists(ObjectPath);
        if (dirExists) {
            System.out.println("Directory Alerady Exists");
        } else {
            try {
                Files.createDirectories(RepositoryPath);
                Files.createDirectories(RootFolderPath);
                Files.createDirectories(ObjectPath);
                Files.createDirectories(branchesPath);
                Files.createFile(activeBranchePath);
                Files.createFile(branchesNamesPath);
                Files.createFile(rootFolderNamePath);
                Files.createFile(commitStatusPath);
                Files.createFile(materFilePath);
                Files.write(activeBranchePath, "master".getBytes());
                Files.write(branchesNamesPath, "master".getBytes());
                Files.write(materFilePath, "".getBytes());
                Files.write(rootFolderNamePath, RepoName.getBytes());
            } catch (IOException ioExceptionObj) {
            }
        }
    }
}
