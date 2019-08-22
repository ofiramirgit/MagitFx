package Logic;

import Logic.Objects.BlobData;
import Logic.Objects.Commit;
import Logic.Objects.Folder;
import Zip.ZipFile;
import inputValidation.FilesValidation;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;

import static Logic.ConstantsEnums.EmptyString;
import static Logic.ConstantsEnums.Separator;

public class Logic {

    FilesValidation m_FilesValidation = new FilesValidation();
    private String m_ActiveUser;
    private String m_ActiveRepository;
    private String m_ActiveRepositoryName;
    private ZipFile m_ZipFile;
    private Map<String, String> m_CurrentCommitStateMap;
    private InputValidation m_InputValidation = new InputValidation();


    //---------------Getters&Setters-------------------------------------------------------
    public void setM_ActiveUser(String i_ActiveUser) {
        m_ActiveUser = i_ActiveUser;
    }

    public void setM_ActiveRepositoryName(String m_ActiveRepositoryName) {
        this.m_ActiveRepositoryName = m_ActiveRepositoryName;
    }

    //-------initRepository-------start------------------------------------------------------------------

    public boolean initRepository(String selectedFolder,String RepositoryName) {
        if(m_FilesValidation.isRepository(selectedFolder + File.separator + RepositoryName))
            return false;
        else
            createRepository(selectedFolder, RepositoryName);
        return true;
    }

    private void createRepository(String selectedFolder,String RepositoryName) {
        Path RepositoryPath = Paths.get(selectedFolder + File.separator + RepositoryName);
        Path RootFolderPath = Paths.get(selectedFolder + File.separator + RepositoryName + File.separator + RepositoryName);
        Path ObjectPath = Paths.get(selectedFolder + File.separator + RepositoryName + File.separator + ".magit" + File.separator + "objects");
        Path branchesPath = Paths.get(selectedFolder + File.separator + RepositoryName + File.separator + ".magit" + File.separator + "branches");
        Path activeBranchePath = Paths.get(selectedFolder+ File.separator + RepositoryName + File.separator + ".magit" + File.separator + "branches" + File.separator + "HEAD.txt");
        Path branchesNamesPath = Paths.get(selectedFolder + File.separator + RepositoryName + File.separator + ".magit" + File.separator + "branches" + File.separator + "NAMES.txt");
        Path rootFolderNamePath = Paths.get(selectedFolder + File.separator + RepositoryName + File.separator + ".magit" + File.separator + "RootFolderName.txt");
        Path commitStatusPath = Paths.get(selectedFolder + File.separator + RepositoryName + File.separator + ".magit" + File.separator + "CommitStatus.txt");
        Path materFilePath = Paths.get(selectedFolder + File.separator + RepositoryName + File.separator + ".magit" + File.separator + "branches" +File.separator +"mater.txt");
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
                Files.write(rootFolderNamePath, RepositoryName.getBytes());
            } catch (IOException ioExceptionObj) {
            }
        }
    }

    //-------initRepository-------end--------

    //-------Read XML-------end--------

    public void readXML(String i_XmlFilePath) throws XmlException {
        XmlReader xmlReader = new XmlReader(i_XmlFilePath);

        String[] RepositoryLocation = xmlReader.getLocation();
        m_ActiveRepository = RepositoryLocation[0] + File.separator + RepositoryLocation[1];
        setM_ActiveRepositoryName(RepositoryLocation[1]);
        if (!m_InputValidation.checkInputActiveRepository(RepositoryLocation[0] + File.separator + RepositoryLocation[1]))
            initRepositoryXML(RepositoryLocation); //*********************************
        else
            throw new XmlException("Repository Already Exist.",RepositoryLocation[0] + File.separator + RepositoryLocation[1]);
        xmlReader.buildFromXML();
        spreadCommitToWc(xmlReader.getActiveBranch());
    }

    private void spreadCommitToWc(String i_BranchName) {
        File BranchFile = new File(getPathFolder("branches") + File.separator + i_BranchName + ".txt");

        if (!getContentOfFile(BranchFile).equals(EmptyString)) {
            String sha1OfLastCommitBranch = getContentOfFile(BranchFile);
            Commit commit = new Commit(getContentOfZipFile(getPathFolder("objects"), sha1OfLastCommitBranch));
            String RootFolderSha1 = commit.getM_MainSHA1();
            String path = m_ActiveRepository + File.separator + getRootFolderName();
            buildingRepository(path, RootFolderSha1, ConstantsEnums.FileType.FOLDER);
        }
    }

    public void initRepositoryXML(String []i_RepositoryArgs) {
        Path RepositoryPath = Paths.get(i_RepositoryArgs[0] + File.separator + i_RepositoryArgs[1]);
        Path RootFolderPath = Paths.get(i_RepositoryArgs[0] + File.separator + i_RepositoryArgs[1] + File.separator + i_RepositoryArgs[1]);
        Path ObjectPath = Paths.get(i_RepositoryArgs[0] + File.separator + i_RepositoryArgs[1] + File.separator + ".magit" + File.separator + "objects");
        Path branchesPath = Paths.get(i_RepositoryArgs[0] + File.separator + i_RepositoryArgs[1] + File.separator + ".magit" + File.separator + "branches");
        Path activeBranchePath = Paths.get(i_RepositoryArgs[0] + File.separator + i_RepositoryArgs[1] + File.separator + ".magit" + File.separator + "branches" + File.separator + "HEAD.txt");
        Path branchesNamesPath = Paths.get(i_RepositoryArgs[0] + File.separator + i_RepositoryArgs[1] + File.separator + ".magit" + File.separator + "branches" + File.separator + "NAMES.txt");
        Path rootFolderNamePath = Paths.get(i_RepositoryArgs[0] + File.separator + i_RepositoryArgs[1] + File.separator + ".magit" + File.separator + "RootFolderName.txt");
        Path commitStatusPath = Paths.get(i_RepositoryArgs[0] + File.separator + i_RepositoryArgs[1] + File.separator + ".magit" + File.separator + "CommitStatus.txt");
        Path materFilePath = Paths.get(i_RepositoryArgs[0] + File.separator + i_RepositoryArgs[1] + File.separator + ".magit" + File.separator + "branches" +File.separator +"mater.txt");
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
                Files.write(materFilePath, EmptyString.getBytes());
                Files.write(rootFolderNamePath, i_RepositoryArgs[1].getBytes());

                m_ActiveRepository = i_RepositoryArgs[0] + File.separator + i_RepositoryArgs[1];
                setM_ActiveRepositoryName(i_RepositoryArgs[1]);
            } catch (IOException ioExceptionObj) {
            }
        }
    }

    //-------Read XML-------end--------

    //-----------Files&Folders-------------------------------------

    private String getContentOfFile(File i_File) {
        String fileContent = EmptyString;
        Path path = Paths.get(i_File.getAbsolutePath());

        try {
            fileContent=EmptyString;
            if(path.toFile().length()>0)
                fileContent = new String(Files.readAllBytes(path));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return fileContent;
    }

    private String getContentOfZipFile(String i_Path, String i_ZipName) {
        m_ZipFile.unZipIt(i_Path + File.separator + i_ZipName + ".zip", i_Path);
        File unZippedFile = new File(i_Path + File.separator + i_ZipName + ".txt");
        String contentOfFile = getContentOfFile(unZippedFile);
        unZippedFile.delete();
        return contentOfFile;
    }

    public String getPathFolder(String i_Folder) {
        String path = EmptyString;
        switch (i_Folder) {
            case "objects":
                path = m_ActiveRepository + File.separator + ".magit" + File.separator + "objects";
                break;
            case "branches":
                path = m_ActiveRepository + File.separator + ".magit" + File.separator + "branches";
                break;
            case ".magit":
                path = m_ActiveRepository + File.separator + ".magit";
                break;
        }
        return path;
    }

    //--------------Genarel----------------------------------------

    private String getRootFolderName() {
        Path RootFolderName = Paths.get(getPathFolder(".magit") + File.separator + "RootFolderName.txt");
        if(Files.exists(RootFolderName))
            return getContentOfFile(new File(getPathFolder(".magit") + File.separator + "RootFolderName.txt"));
        else
            return EmptyString;
    }

    private void buildingRepository(String path, String Sha1, ConstantsEnums.FileType i_FileType) {
        if (i_FileType == ConstantsEnums.FileType.FOLDER) {
            File pathFile = new File(path);
            pathFile.mkdir();
            Folder folder = new Folder(getContentOfZipFile(getPathFolder("objects"), Sha1));
            List<BlobData> BlobDataArray = folder.getLibraryFiles();
            for (BlobData blobData : BlobDataArray) {
                buildingRepository(path + File.separator + blobData.getM_Name(), blobData.getM_Sha1(), blobData.getM_Type());
            }
        } else {
            Path pathFile = Paths.get(path);
            try {
                Files.createFile(pathFile);
                Files.write(pathFile, getContentOfZipFile(getPathFolder("objects"), Sha1).getBytes());

                String toFile = pathFile.toFile().getAbsolutePath() + Separator + Sha1 + System.lineSeparator();
                Path pathToFile = Paths.get(getPathFolder(".magit") + File.separator + "CommitStatus.txt");
                try {
                    Files.write(pathToFile, toFile.getBytes(), StandardOpenOption.APPEND);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }


}
