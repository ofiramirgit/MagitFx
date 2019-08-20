package Logic;

import inputValidation.FilesValidation;

import javax.sql.rowset.spi.XmlReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Logic {

    FilesValidation m_FilesValidation = new FilesValidation();
    private String m_ActiveUser;
    private String m_ActiveRepository;

    //---------------Getters&Setters-------------------------------------------------------
    public void setM_ActiveUser(String i_ActiveUser) {
        m_ActiveUser = i_ActiveUser;
    }

    public void setM_ActiveRepositoryName(String m_ActiveRepositoryName) {
        this.m_ActiveRepositoryName = m_ActiveRepositoryName;
    }

    //-------initRepository-------start------------------------------------------------------------------

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

    //-------initRepository-------end--------

    //-------Read XML-------end--------

    public void readXML(String i_XmlFilePath) throws XmlException {
        XmlReader xmlReader = new XmlReader(i_XmlFilePath);

        String[] RepositoryLocation = xmlReader.getLocation();
        setM_ActiveRepository(RepositoryLocation[0] + File.separator + RepositoryLocation[1]);
        setM_ActiveRepositoryName(RepositoryLocation[1]);
        if (!m_InputValidation.checkInputActiveRepository(RepositoryLocation[0] + File.separator + RepositoryLocation[1]))
            initRepository(RepositoryLocation);
        else
            throw new XmlException("Repository Already Exist.",RepositoryLocation[0] + File.separator + RepositoryLocation[1]);
        xmlReader.buildFromXML();
        spreadCommitToWc(xmlReader.getActiveBranch());
    }

    //-------Read XML-------end--------

}
