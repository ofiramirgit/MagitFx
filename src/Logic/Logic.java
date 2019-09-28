package Logic;

import Logic.Objects.*;
import Zip.ZipFile;
import inputValidation.FilesValidation;
import node.CommitNode;
import org.apache.commons.codec.digest.DigestUtils;
//import sun.security.ec.ECDSASignature;

import javax.xml.bind.JAXBException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.text.ParseException;
import java.util.*;

import static Logic.ConstantsEnums.*;
import static Logic.ConstantsEnums.FileState.*;
import static Logic.ConstantsEnums.dateFormat;

public class Logic {

    private FilesValidation m_FilesValidation = new FilesValidation();
    private String m_ActiveUser;
    private String m_ActiveRepository;
    private ZipFile m_ZipFile;
    private Map<String, String> m_CurrentCommitStateMap;
    private InputValidation m_InputValidation = new InputValidation();
    private String m_CollaborateWithPath = EmptyString;
    private String m_CollaborateName;

    public Logic() {
        m_ActiveUser = "Administrator";
        m_ActiveRepository = EmptyString;
        m_ZipFile = new ZipFile();
        m_CurrentCommitStateMap = new HashMap<>();
    }

    public String getM_ActiveRepository() {
        return m_ActiveRepository;
    }
    //-------------------------------------------REPOSITORY  START----------------------------------

    //-------initRepository-------Start------
    public boolean initRepository(String selectedFolder, String RepositoryName) {
        if (m_FilesValidation.isRepository(selectedFolder + File.separator + RepositoryName))
            return false;
        else
            createRepository(selectedFolder, RepositoryName);
        return true;
    }

    //todo duplicate
    private void createRepository(String selectedFolder, String RepositoryName) {
        m_ActiveRepository = selectedFolder + File.separator + RepositoryName;
        Path RepositoryPath = Paths.get(selectedFolder + File.separator + RepositoryName);
        Path RootFolderPath = Paths.get(selectedFolder + File.separator + RepositoryName + File.separator + RepositoryName);
        Path ObjectPath = Paths.get(selectedFolder + File.separator + RepositoryName + File.separator + ".magit" + File.separator + "objects");
        Path branchesPath = Paths.get(selectedFolder + File.separator + RepositoryName + File.separator + ".magit" + File.separator + "branches");
        Path activeBranchePath = Paths.get(selectedFolder + File.separator + RepositoryName + File.separator + ".magit" + File.separator + "branches" + File.separator + "HEAD.txt");
        Path branchesNamesPath = Paths.get(selectedFolder + File.separator + RepositoryName + File.separator + ".magit" + File.separator + "branches" + File.separator + "NAMES.txt");
        Path rootFolderNamePath = Paths.get(selectedFolder + File.separator + RepositoryName + File.separator + ".magit" + File.separator + "RootFolderName.txt");
        Path commitStatusPath = Paths.get(selectedFolder + File.separator + RepositoryName + File.separator + ".magit" + File.separator + "CommitStatus.txt");
        Path materFilePath = Paths.get(selectedFolder + File.separator + RepositoryName + File.separator + ".magit" + File.separator + "branches" + File.separator + "mater.txt");
        Boolean dirExists = Files.exists(ObjectPath);
        if (dirExists) {
            //todo alert
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
                //todo catch
            }
        }
    }
    //-------initRepository-------End--------

    //-------Read XML-------------Start---------
    public void readXML(String i_XmlFilePath) throws XmlException {
        XmlReader xmlReader = new XmlReader();
        try {
            xmlReader.ReadXml(i_XmlFilePath);
        } catch (JAXBException e) {
            e.printStackTrace();
        }

        String[] RepositoryLocation = xmlReader.getLocation();
        m_ActiveRepository = RepositoryLocation[0] + File.separator + RepositoryLocation[1];
          if (!m_InputValidation.checkInputActiveRepository(m_ActiveRepository))
              createRepository(RepositoryLocation[0],RepositoryLocation[1]);
          else
              throw new XmlException("Repository Already Exist.", m_ActiveRepository);
        xmlReader.buildFromXML();
        spreadCommitToWc(xmlReader.getActiveBranch());
    }
    //-------Read XML-------------End-----------

    //-------Switch Repository-----Start--------
    public Boolean setM_ActiveRepository(String i_ActiveRepository) {
        if (m_FilesValidation.isRepository(i_ActiveRepository)) {
            m_ActiveRepository = i_ActiveRepository;
            return true;
        }
        return false;
    }
    //-------Switch Repository-----End----------

    //-------Set User Name---------Start--------
    public Boolean setM_ActiveUser(String i_ActiveUser) {
        if (m_InputValidation.checkInputStringLen(i_ActiveUser)) {
            m_ActiveUser = i_ActiveUser;
            return true;
        }
        return false;
    }
    //-------Set User Name---------End--------

    //-------------------------------------------REPOSITORY  END-------------------------------------


    //-------------------------------------------COMMIT  START-------------------------------------

    //-------Commit---------Start--------
    public void createCommit(String i_Msg) {
        WorkingCopyStatus wcStatus = ShowWorkingCopyStatus(getPathFolder(".magit") + File.separator + "CommitStatus.txt");
        String objectFolder;
        if (!wcStatus.IsEmpty()) {
            Commit newCommit = new Commit();
            newCommit.setM_Message(i_Msg);
            newCommit.setM_CreatedBy(m_ActiveUser);
            newCommit.setM_PreviousSHA1("NONE");
            newCommit.setM_PreviousSHA1merge("NONE");
            newCommit.setM_CreatedTime(dateFormat.format(new Date()));

            String rootFolderName = getRootFolderName();
            File rootFolderFile = new File(m_ActiveRepository + File.separator + rootFolderName);
            File commitStatusFile = new File(getPathFolder(".magit") + File.separator + "CommitStatus.txt");

            try {
                Files.write(Paths.get(commitStatusFile.getAbsolutePath()), "".getBytes(), StandardOpenOption.TRUNCATE_EXISTING);

            } catch (IOException e) {
                e.printStackTrace();
            }

            objectFolder = getPathFolder("objects");

            if (!isFirstCommit()) {
                String PreviousSHA1 = getContentOfFile(new File(getPathFolder("branches"), getBranchActiveName() + ".txt"));
                newCommit.setM_PreviousSHA1(PreviousSHA1);
            }

            BlobData rootBlobData = recursiveTravelFolders(objectFolder, rootFolderFile, wcStatus);//throw all the WC files to WcCommit folder
            newCommit.setM_MainSHA1(rootBlobData.getM_Sha1());
            String commitSha1 = DigestUtils.sha1Hex(newCommit.toString());
            m_ZipFile.zipFile(objectFolder, commitSha1, newCommit.toString());
            updateBranchActiveCommit(commitSha1);
        }
    }

    private BlobData recursiveTravelFolders(String i_FolderToZipInto, File i_File, WorkingCopyStatus i_WCstatus) {
        String sha1;
        BlobData newBlobData;
        if (i_File.isDirectory()) {
            Folder folder = new Folder();
            for (final File f : i_File.listFiles()) {//todo file nullpointer
                if (!(f.isDirectory() && f.listFiles().length == 0)) {//todo file nullpointer
                    folder.AddNewItem(recursiveTravelFolders(i_FolderToZipInto, f, i_WCstatus));
                } else {
                    deleteFolder(f);
                }
            }

            sha1 = DigestUtils.sha1Hex(folder.toString());
            BlobData directoryBlob =
                    new BlobData(i_File.getName(), sha1, ConstantsEnums.FileType.FOLDER,
                            m_ActiveUser, dateFormat.format(new Date())
                    );
            m_ZipFile.zipFile(i_FolderToZipInto, sha1, folder.printArray());

            return directoryBlob;
        } else { //isFile
            Boolean fileChanged = false;

            Blob blob = new Blob(getContentOfFile(i_File));
            sha1 = DigestUtils.sha1Hex(blob.getM_Data());

            fileChanged = isFileNeedCommit(i_File.getAbsolutePath(), i_WCstatus);

            if (fileChanged) {
                m_ZipFile.zipFile(i_FolderToZipInto, sha1, getContentOfFile(i_File));
            }

            String toFile = i_File.getAbsolutePath() + Separator + sha1 + System.lineSeparator();
            Path path = Paths.get(getPathFolder(".magit") + File.separator + "CommitStatus.txt");
            try {
                Files.write(path, toFile.getBytes(), StandardOpenOption.APPEND);
            } catch (IOException e) {
                e.printStackTrace();
            }

            newBlobData = new BlobData(i_File.getName(), sha1, ConstantsEnums.FileType.FILE,
                    m_ActiveUser, dateFormat.format(new Date()));
        }
        return newBlobData;
    }

    private Boolean isFileNeedCommit(String i_Path, WorkingCopyStatus i_WCstatus) {
        return i_WCstatus.getM_NewFilesList().contains(i_Path) || i_WCstatus.getM_ChangedFilesList().contains(i_Path);
    }

    private boolean isFirstCommit() {
        String BranchName = getBranchActiveName();
        return (getContentOfFile(new File(getPathFolder("branches") + File.separator + BranchName + ".txt")).equals(EmptyString));
    }

    private void updateBranchActiveCommit(String i_CommitSha1) {
        String activeBranchName = getBranchActiveName();
        Path activeBranchPath = Paths.get(getPathFolder("branches") + File.separator + activeBranchName + ".txt");
        try {
            if (!Files.exists(activeBranchPath))
                Files.createFile(activeBranchPath);
            Files.write(activeBranchPath, i_CommitSha1.getBytes());
        } catch (Exception ex) {//todo

        }
    }
    //-------Commit---------End--------

    //-------Working Copy---------Start--------
    public WorkingCopyStatus ShowWorkingCopyStatus(String pathCommitStatus) {

        WorkingCopyStatus wcStatus = new WorkingCopyStatus();
        String rootFolderName = getRootFolderName();
        File commitStatusFile = new File(pathCommitStatus);
        m_CurrentCommitStateMap.clear();

        if (!getContentOfFile(commitStatusFile).isEmpty()) {
            String[] commitStatusArr = getContentOfFile(commitStatusFile).split(System.lineSeparator());
            for (String s : commitStatusArr) {
                String[] strings = s.split(Separator);
                m_CurrentCommitStateMap.put(strings[0], strings[1]);
            }
        }

        wcStatus.setM_DeletedFilesList(m_CurrentCommitStateMap.keySet());
        wcStatus.setM_NotChangedFilesList(m_CurrentCommitStateMap.keySet());

        recursiveCompareWC(m_ActiveRepository, rootFolderName, wcStatus);

        for (String str : wcStatus.getM_ChangedFilesList())
            wcStatus.getM_NotChangedFilesList().remove(str);
        for (String str : wcStatus.getM_NewFilesList())
            wcStatus.getM_NotChangedFilesList().remove(str);
        for (String str : wcStatus.getM_DeletedFilesList())
            wcStatus.getM_NotChangedFilesList().remove(str);

        return wcStatus;
    }

    private void recursiveCompareWC(String stringPath, String fName, WorkingCopyStatus i_WcStatus) {
        File file = new File(stringPath + File.separator + fName);

        if (file.isDirectory()) {
            for (File f : file.listFiles()) {//todo file null pointer
                recursiveCompareWC(file.getAbsolutePath(), f.getName(), i_WcStatus);
            }
        } else // isFile
        {
            String sha1InCommit = m_CurrentCommitStateMap.get(file.getAbsolutePath());
            String sha1InWC = DigestUtils.sha1Hex(getContentOfFile(file));

            i_WcStatus.getM_DeletedFilesList().remove(file.getAbsolutePath());

            if (sha1InCommit == null) {
                i_WcStatus.getM_NewFilesList().add(file.getAbsolutePath());
            } else if (!sha1InCommit.equals(sha1InWC)) {
                i_WcStatus.getM_ChangedFilesList().add(file.getAbsolutePath());
            }

        }
    }
    //-------Working Copy---------End--------

    //-------------------------------------------COMMIT  END-------------------------------------


    //-------------------------------------------BRANCH  START-------------------------------------

    //-------Show Branch List---------Start--------
    public List<BranchData> GetAllBranchesDetails() {
        File branchesNamesFile = new File(getPathFolder("branches") + File.separator + "NAMES.txt");
        List<BranchData> branchDataList = new ArrayList<>();
        String[] branchesNamesArray = getContentOfFile(branchesNamesFile).split(System.lineSeparator());

        for (String branchName : branchesNamesArray) {
            if (!getContentOfFile(new File(getPathFolder("branches"), branchName + ".txt")).equals(EmptyString)) {
                String commitSha1 = getContentOfFile(new File(getPathFolder("branches"), branchName + ".txt"));
                Commit commit = new Commit(getContentOfZipFile(getPathFolder("objects"), commitSha1));

                branchDataList.add(new BranchData(branchName, getBranchActiveName().equals(branchName),
                        commitSha1, commit.getM_Message()));
            }
        }
        return branchDataList;
    }
    //-------Show Branch List---------End----------

    //-------Check Out Head Branch---------Start--------
    public void CheckOutHeadBranch(String i_BranchName, Boolean i_toCommit, String Msg) {
        if (i_toCommit)
            createCommit(Msg);

        File rootFolder = new File(m_ActiveRepository + File.separator + getRootFolderName());
        deleteFolder(rootFolder);
        spreadCommitToWc(i_BranchName);
        Path HeadFile = Paths.get(getPathFolder("branches") + File.separator + "HEAD.txt");
        try {
            Files.write(HeadFile, i_BranchName.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //-------Check Out Head Branch---------END--------

    //-------CreateNewBranch---------Start--------
    public Boolean createNewBranch(String i_BranchName) {
        Boolean branchNameNotExist = true;//todo boolean primitive
        String stringPath = getPathFolder("branches") + File.separator + "NAMES.txt";
        File branchesNamesFile = new File(stringPath);
        String branchesNames = getContentOfFile(branchesNamesFile);
        String[] names = branchesNames.split(System.lineSeparator());
        for (String name : names) {
            if (name.equals(i_BranchName))
                branchNameNotExist = false;
        }
        if (branchNameNotExist) {
            String Sha1CurrentBranch;
            Path path = Paths.get(stringPath);
            Path BranchPath = Paths.get(getPathFolder("branches") + File.separator + i_BranchName + ".txt");
            Sha1CurrentBranch = getContentOfFile(new File(getPathFolder("branches") + File.separator + getBranchActiveName() + ".txt"));
            try {
                Files.createFile(BranchPath);
                Files.write(BranchPath, Sha1CurrentBranch.getBytes());
                Files.write(path, (System.lineSeparator() + i_BranchName).getBytes(), StandardOpenOption.APPEND);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return branchNameNotExist;
    }
    //-------CreateNewBranch---------End--------

    //-------DeleteExistBranch---------Start--------
    public Boolean deleteBranch(String i_BranchName) {
        Boolean bool = false;//todo boolean primitive
        if (getBranchActiveName().equals(i_BranchName))
            bool = false;
        else {
            try {
                File branchesNamesFile = new File(getPathFolder("branches") + File.separator + "NAMES.txt");
                String branchesNamesContent = getContentOfFile(branchesNamesFile);
                if (!branchesNamesContent.contains(i_BranchName))
                    bool = false;
                else {
                    branchesNamesFile.delete();//todo delete return value
                    branchesNamesContent = branchesNamesContent.replace(System.lineSeparator() + i_BranchName, "");
                    Path path = Paths.get(getPathFolder("branches") + File.separator + "NAMES.txt");
                    Files.write(path, branchesNamesContent.getBytes(), StandardOpenOption.CREATE);
                    bool = true;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bool;
    }
    //-------DeleteExistBranch---------End--------

    //-------MergeBranches---------Start--------
    public OpenAndConflict MergeBranches(String oursBranch, String theirsBranch) {
        String sharedFatherSha1 = findSharedFather(oursBranch, theirsBranch);
        String Sha1Theirs = getContentOfFile(new File(getPathFolder("branches"), theirsBranch + ".txt"));
        String ActiveBranch = getBranchActiveName();


        try {
            Files.createDirectories(Paths.get(getPathFolder("merge")));
        } catch (IOException e) {
            e.printStackTrace();
        }
        spreadFolder(sharedFatherSha1, "Father");
        WorkingCopyStatus wcOurs = ShowWorkingCopyStatus(getPathFolder("merge") + File.separator + "FatherCommitStatus.txt");

        spreadFolder(Sha1Theirs, "Theirs");
        CheckOutHeadBranch(theirsBranch, false, "");
        WorkingCopyStatus wcTheirs = ShowWorkingCopyStatus(getPathFolder("merge") + File.separator + "FatherCommitStatus.txt");

        CheckOutHeadBranch(ActiveBranch, false, "");

        ArrayList<OpenChange> OpenChanges = new ArrayList<>();
        ArrayList<Conflict> ConflictFiles = new ArrayList<>();

        fillConflictAndOpenChangesList(wcOurs, wcTheirs, OpenChanges, ConflictFiles);
        return (new OpenAndConflict(ConflictFiles, OpenChanges));
    }

    private String findSharedFather(String oursBranch, String theirsBranch) {
        String Sha1Ours = getContentOfFile(new File(getPathFolder("branches"), oursBranch + ".txt"));
        String Sha1Theirs = getContentOfFile(new File(getPathFolder("branches"), theirsBranch + ".txt"));
        System.out.println("ours: " + Sha1Ours + "   " + "theirs: " + Sha1Theirs);

        while (true) {
            if (Sha1Ours.equals(Sha1Theirs)) {
                System.out.println("Sha1Father:    " + Sha1Ours);
                break;
            } else {
                Commit commitOurs = new Commit(getContentOfZipFile(getPathFolder("objects"), Sha1Ours));
                Commit commitTheirs = new Commit(getContentOfZipFile(getPathFolder("objects"), Sha1Theirs));
                System.out.println("commitOurs:    " + commitOurs);
                System.out.println("commitTheirs:  " + commitTheirs);
                try {
                    Date OursDate = dateFormat.parse(commitOurs.getM_CreatedTime());
                    Date TheirsDate = dateFormat.parse(commitTheirs.getM_CreatedTime());
                    if (OursDate.compareTo(TheirsDate) < 0)
                        Sha1Theirs = commitTheirs.getM_PreviousSHA1();
                    else
                        Sha1Ours = commitOurs.getM_PreviousSHA1();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
        return Sha1Ours;
    }

    private void fillConflictAndOpenChangesList(WorkingCopyStatus wcOurs, WorkingCopyStatus wcTheirs,
                                                ArrayList<OpenChange> openChanges, ArrayList<Conflict> conflictFiles) {
        List<String> newOurs = wcOurs.getM_NewFilesList();
        List<String> newTheirs = wcTheirs.getM_NewFilesList();
        List<String> updatedOurs = wcOurs.getM_ChangedFilesList();
        List<String> updatedTheirs = wcTheirs.getM_ChangedFilesList();
        Set<String> deletedOurs = wcOurs.getM_DeletedFilesList();
        Set<String> deletedTheirs = wcTheirs.getM_DeletedFilesList();
        List<String> NotChangedOurs = wcOurs.getM_NotChangedFilesList();
        List<String> NotChangedTheirs = wcTheirs.getM_NotChangedFilesList();
        for (String newFile : newOurs) {//created files
            if (newTheirs.contains(newFile)) {//if created in ours and theirs
                Path newPathFileTheirs = Paths.get(newFile.replace(m_ActiveRepository + File.separator + getRootFolderName(), getPathFolder("merge") + File.separator + "Theirs"));
                if (DigestUtils.sha1Hex(getContentOfFile(newPathFileTheirs.toFile()))
                        .equals(DigestUtils.sha1Hex(getContentOfFile(new File(newFile))))) {

                    //if created the same file
                    openChanges.add(new OpenChange(Paths.get(newFile), CREATED));
                } else {//if created different files but same name
                    conflictFiles.add(new Conflict(
                            new FileStruct(Paths.get(newFile), CREATED),
                            new FileStruct(newPathFileTheirs, CREATED),
                            new FileStruct(Paths.get(EmptyString), NONE)));
                }
                newTheirs.remove(newFile);
            } else {//if new file on ours and not created in theirs
                openChanges.add(new OpenChange(Paths.get(newFile), CREATED));
            }
        }
        for (String deleteFile : deletedOurs) {//delete
            Path deletePathFileTheirs = Paths.get(deleteFile.replace(m_ActiveRepository + File.separator + getRootFolderName(), getPathFolder("merge") + File.separator + "Theirs"));
            Path deletePathFileFather = Paths.get(deleteFile.replace(m_ActiveRepository + File.separator + getRootFolderName(), getPathFolder("merge") + File.separator + "Father"));

            if (deletedTheirs.contains(deleteFile)) {//file deleted in ours and theirs
                openChanges.add(new OpenChange(Paths.get(deleteFile), DELETED));
                deletedTheirs.remove(deleteFile);
            } else {//if file deleted in ours and not deleted in theirs

                if (DigestUtils.sha1Hex(getContentOfFile(deletePathFileTheirs.toFile()))
                        .equals(DigestUtils.sha1Hex(getContentOfFile(deletePathFileFather.toFile())))) {//file in theirs is the same in father
                    openChanges.add(new OpenChange(deletePathFileTheirs, DELETED));
                } else {//file in theirs no the same in father
                    conflictFiles.add(new Conflict(
                            new FileStruct(Paths.get(deleteFile), DELETED),
                            new FileStruct(deletePathFileTheirs, UPDATED),
                            new FileStruct(deletePathFileFather, NOTCHANGED)
                    ));
                }
            }
        }
        for (String updateFile : updatedOurs) {//updated
            Path updatePathFileTheirs = Paths.get(updateFile.replace(m_ActiveRepository + File.separator + getRootFolderName(), getPathFolder("merge") + File.separator + "Theirs"));
            Path updatePathFileFather = Paths.get(updateFile.replace(m_ActiveRepository + File.separator + getRootFolderName(), getPathFolder("merge") + File.separator + "Father"));
            if (updatedTheirs.contains(updateFile)) {//file updated in ours and theirs
                if (DigestUtils.sha1Hex(getContentOfFile(updatePathFileTheirs.toFile()))
                        .equals(DigestUtils.sha1Hex(getContentOfFile(new File(updateFile))))) {
                    //if updated the same file and content
                    openChanges.add(new OpenChange(Paths.get(updateFile), CREATED));
                } else {//updated in both but different content
                    conflictFiles.add(new Conflict(
                            new FileStruct(Paths.get(updateFile), UPDATED),
                            new FileStruct(updatePathFileTheirs, UPDATED),
                            new FileStruct(updatePathFileFather, NOTCHANGED)));
                }
                updatedTheirs.remove(updateFile);
            } else {//file updated in ours and not in theirs
                openChanges.add(new OpenChange(Paths.get(updateFile), UPDATED));
            }
        }
        for (String notChanged : NotChangedOurs) {//notChanged
            if (NotChangedTheirs.contains(notChanged)) {//file updated in ours and theirs
                openChanges.add(new OpenChange(Paths.get(notChanged), NOTCHANGED));
                NotChangedTheirs.remove(notChanged);
            }
        }
        for (String newFile : newTheirs) {
            Path updatePathFileTheirs = Paths.get(newFile.replace(m_ActiveRepository + File.separator + getRootFolderName(), getPathFolder("merge") + File.separator + "Theirs"));
            openChanges.add(new OpenChange(updatePathFileTheirs, CREATED));
        }
    }
    //-------mergeBranches---------End----------

    //-------------------------------------------BRANCH  END-------------------------------------
    //-------------------------------------------COLLABORATION START-------------------------------------
    //Clone
    public void Clone(String i_Source, String i_Destination, String i_repoName) {

        File sourceFolder = new File(i_Source);
        File destinationFolder = new File(i_Destination + File.separator + i_repoName);
        try {
            copyFolder(sourceFolder, destinationFolder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        createRemoteBranches(destinationFolder, sourceFolder);
        m_ActiveRepository = destinationFolder.getAbsolutePath();
        m_CollaborateWithPath = i_Source;
        m_CollaborateName = sourceFolder.getName();
    }

    private void createRemoteBranches(File destinationFolder, File sourceFolder) {
        File branchDirectory = new File(destinationFolder + File.separator + ".magit" + File.separator + "branches");
        Path RBfolder = Paths.get(branchDirectory.getAbsolutePath() + File.separator + sourceFolder.getName());
        Path RTBfol = Paths.get(branchDirectory.getAbsolutePath() + File.separator + "RTB");
        String headBranchName;

        try {
            Files.createDirectory(RBfolder);
        } catch (IOException e) {
            e.printStackTrace();
        }

       //create all RB's
        for (File branch : branchDirectory.listFiles()) {
            if(!branch.getName().equals("NAMES.txt") && !branch.getName().equals("HEAD.txt") && !branch.getName().equals(sourceFolder.getName())){
                try {
                    File newBranch = new File(RBfolder.toString() + File.separator + branch.getName());
                    if (!newBranch.exists())
                    {
                        newBranch.createNewFile();
                    }
                    Files.copy(branch.toPath(), newBranch.toPath() , StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        //create RTB
        try {
            Files.createDirectory(RTBfol);
            Path headPath = Paths.get(branchDirectory.getAbsolutePath() + File.separator + "HEAD.txt");
            headBranchName = new String(Files.readAllBytes(headPath));
            File activeBranch = new File(branchDirectory.toPath() + File.separator + headBranchName + ".txt");
            File newRTB = new File(RTBfol.toString() + File.separator + headBranchName + ".txt");
            if (!newRTB.exists())
            {
                Files.copy(activeBranch.toPath(), newRTB.toPath() , StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void copyFolder(File sourceFolder, File destinationFolder) throws IOException {
        //Check if sourceFolder is a directory or file
        //If sourceFolder is file; then copy the file directly to new location
        if (sourceFolder.isDirectory())
        {
            //Verify if destinationFolder is already present; If not then create it
            if (!destinationFolder.exists())
            {
                destinationFolder.mkdir();
            }

            //Get all files from source directory
            String files[] = sourceFolder.list();

            //Iterate over all files and copy them to destinationFolder one by one
            for (String file : files)
            {
                File srcFile = new File(sourceFolder, file);
                File destFile = new File(destinationFolder, file);

                //Recursive function call
                copyFolder(srcFile, destFile);
            }
        }
        else
        {
            //Copy the file content from one place to another
            Files.copy(sourceFolder.toPath(), destinationFolder.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    //Fetch
    public void Fetch() {
        fetchCommits();
        fetchBranches();

    }

    private void fetchCommits() {
        doFetch("objects","objects");
    }

    private void fetchBranches() {
        doFetch("branches", "branches" + File.separator + m_CollaborateName);
        doFetch("branches", "branches");
    }

    private void doFetch(String i_From, String i_To){
        File sourceFile = new File(m_CollaborateWithPath + File.separator + ".magit" + File.separator + i_From);
        File destinationFile = new File(m_ActiveRepository + File.separator + ".magit" + File.separator + i_To);

        for (File file : sourceFile.listFiles()) {
            try {
                Path destination = Paths.get(destinationFile.getAbsolutePath() + File.separator +file.getName());
                Files.copy(file.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //Pull
    public void Pull(){
       String headBranchNameRR;
       Path headPathRR = Paths.get(m_CollaborateWithPath + File.separator + ".magit" + File.separator +"branches" + File.separator + "HEAD.txt");
       Path objectsLR = Paths.get(m_ActiveRepository + File.separator + ".magit" + File.separator + "objects");
       try {
            headBranchNameRR = new String(Files.readAllBytes(headPathRR));
            getFilesFromCollaborator(headBranchNameRR, objectsLR);
            //update branch
       } catch (IOException e) {
           e.printStackTrace();
       }
    }

    private void getFilesFromCollaborator(String headBranchNameRR, Path objectsLR) {
          Path headBranchPath = Paths.get(m_CollaborateWithPath + File.separator + ".magit" + File.separator +"branches" + File.separator + headBranchNameRR + ".txt");
          try {
              String commitSha1 = new String(Files.readAllBytes(headBranchPath));
              Path objectsRR = Paths.get(m_CollaborateWithPath + File.separator + ".magit" + File.separator + "objects");
              recursiveGetFiles(objectsRR, objectsLR, commitSha1, FileType.COMMIT);
          } catch (IOException e) {
              e.printStackTrace();
          }
    }

    private void recursiveGetFiles(Path i_From, Path i_To, String i_Sha1, FileType i_FileType) {
        String fileDetails = getContentOfZipFile(i_From + File.separator, i_Sha1);
        pullFile(i_From, i_To, i_Sha1);

        if(i_FileType.equals(FileType.COMMIT)){
            String[] commitDetailsArr = fileDetails.split(", ");
            String folderName =commitDetailsArr[0];
            recursiveGetFiles(i_From, i_To, folderName, FileType.FOLDER);

            if(!commitDetailsArr[1].equals("NONE")){
                String prevCommit =commitDetailsArr[1];
                recursiveGetFiles(i_From, i_To, prevCommit, FileType.COMMIT);
            }

        }
        else if(i_FileType.equals(FileType.FOLDER)) {
            String[] folderDetailsArr = fileDetails.split(" ~ ");
            for(String fileString : folderDetailsArr){
                String[] fileDetailsArr = fileString.split(", ");
                if(fileDetailsArr[2].equals("FOLDER")){
                    recursiveGetFiles(i_From, i_To, fileDetailsArr[1], FileType.FOLDER);
                }
                else if(fileDetailsArr[2].equals("FILE")){
                    recursiveGetFiles(i_From, i_To, fileDetailsArr[1], FileType.FILE);
                }
            }
        }
    }

    private void pullFile(Path i_From, Path i_To, String i_sha1) {
        File sourceFile = new File(i_From + File.separator + i_sha1+".zip");
        File destinationFile = new File(i_To + File.separator + i_sha1+".zip");
        try {
            Files.copy(sourceFile.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    //Push
    public void Push() {
    }

    //-------------------------------------------COLLABORATION END-------------------------------------

    //-------------------------------------------GENERAL  START-------------------------------------
    //-------Files&Folders---------Start----------
    public String getRootFolderName() {
        Path RootFolderName = Paths.get(getPathFolder(".magit") + File.separator + "RootFolderName.txt");
        if (Files.exists(RootFolderName))
            return getContentOfFile(new File(getPathFolder(".magit") + File.separator + "RootFolderName.txt"));
        else
            return EmptyString;
    }

    private void deleteFolder(File file) {
        if (file.isDirectory()) {
            for (File f : file.listFiles())//todo file nullpointer
                deleteFolder(f);
            if (!file.getName().equals(getRootFolderName()))
                file.delete();//todo delete return value
        } else
            file.delete();//todo delete return value
    }

    public String getContentOfFile(File i_File) {
        String fileContent = EmptyString;
        Path path = Paths.get(i_File.getAbsolutePath());

        try {
            fileContent = EmptyString;
            if (path.toFile().length() > 0)
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
        unZippedFile.delete();//todo delete return value
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
            case "merge":
                path = m_ActiveRepository + File.separator + ".magit" + File.separator + "merge";
                System.out.println(path);
                break;

        }
        return path;
    }
    //-------Files&Folders---------End----------

    //-------Commit&WC---------Start----------
    private void spreadCommitToWc(String i_BranchName) {
        File BranchFile = new File(getPathFolder("branches") + File.separator + i_BranchName + ".txt");

        if (!getContentOfFile(BranchFile).equals(EmptyString)) {
            String sha1OfLastCommitBranch = getContentOfFile(BranchFile);
            Commit commit = new Commit(getContentOfZipFile(getPathFolder("objects"), sha1OfLastCommitBranch));
            String RootFolderSha1 = commit.getM_MainSHA1();
            String path = m_ActiveRepository + File.separator + getRootFolderName();
            buildingRepository(path, RootFolderSha1, ConstantsEnums.FileType.FOLDER, getPathFolder(".magit") + File.separator + "CommitStatus.txt", true);
        }
    }

    private void spreadFolder(String i_Sha1, String folderName) {
        Commit commit = new Commit(getContentOfZipFile(getPathFolder("objects"), i_Sha1));
        String RootFolderSha1 = commit.getM_MainSHA1();
        String path = getPathFolder("merge") + File.separator + folderName;

        Path folderStatusPath = Paths.get(getPathFolder("merge") + File.separator + folderName + "CommitStatus.txt");
        try {
            Files.createDirectories(Paths.get(path));
            Files.createFile(folderStatusPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        buildingRepository(path, RootFolderSha1, ConstantsEnums.FileType.FOLDER, getPathFolder("merge") + File.separator + folderName + "CommitStatus.txt", false);
    }

    private void buildingRepository(String path, String Sha1, ConstantsEnums.FileType i_FileType, String CommitStatusFile, Boolean FromWc) {
        String pathToWrite;
        if (i_FileType == ConstantsEnums.FileType.FOLDER) {
            File pathFile = new File(path);
            pathFile.mkdir();
            Folder folder = new Folder(getContentOfZipFile(getPathFolder("objects"), Sha1));
            List<BlobData> BlobDataArray = folder.getLibraryFiles();
            for (BlobData blobData : BlobDataArray) {
                buildingRepository(path + File.separator + blobData.getM_Name(), blobData.getM_Sha1(), blobData.getM_Type(), CommitStatusFile, FromWc);
            }
        } else {
            Path pathFile = Paths.get(path);
            try {
                Files.createFile(pathFile);
                Files.write(pathFile, getContentOfZipFile(getPathFolder("objects"), Sha1).getBytes());
                if (FromWc)
                    pathToWrite = pathFile.toFile().getAbsolutePath();
                else {
                    if (pathFile.toFile().getAbsolutePath().contains("Father"))
                        pathToWrite = pathFile.toFile().getAbsolutePath().replace(getPathFolder("merge") + File.separator + "Father", m_ActiveRepository + File.separator + getRootFolderName());
                    else
                        pathToWrite = pathFile.toFile().getAbsolutePath().replace(getPathFolder("merge") + File.separator + "Theirs", m_ActiveRepository + File.separator + getRootFolderName());
                }
                String toFile = pathToWrite + Separator + Sha1 + System.lineSeparator();
                Path pathToFile = Paths.get(CommitStatusFile);
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
    //-------Commit&WC---------End----------

    public boolean WcNotChanged() {
        WorkingCopyStatus workingCopyStatus = ShowWorkingCopyStatus(getPathFolder(".magit") + File.separator + "CommitStatus.txt");
        return workingCopyStatus.isNotChanged();
    }

    public Boolean isBranchExist(String i_BranchName){

        Path BranchPath = Paths.get(getPathFolder("branches") + File.separator + i_BranchName + ".txt");
        if (Files.exists(BranchPath))
            return true;
        return false;
    }

    public String getBranchActiveName() {
        String branchActiveName = EmptyString;

        Path path = Paths.get(getPathFolder("branches") + File.separator + "HEAD.txt");
        try {
            branchActiveName = new String(Files.readAllBytes(path));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return branchActiveName;
    }

    public List<CommitNode> getCommitList() {
        List<BranchDates> branchesList = new ArrayList<>();
        List<CommitNode> commits = new ArrayList<>();

//        List<CommitNode> commitNodeList = null;
        String branchesNames = EmptyString;

        Path path = Paths.get(getPathFolder("branches") + File.separator + "NAMES.txt");
        try {
            branchesNames = new String(Files.readAllBytes(path));
            String Branches[] = branchesNames.split("\r\n");
            for (String branchName : Branches) {
                Path activeBranchPath = Paths.get(getPathFolder("branches") + File.separator + branchName + ".txt");

                if (!Files.exists(activeBranchPath)) {
                    //do nothing
                } else {
                    String brancheCommitSha1 = new String(Files.readAllBytes(activeBranchPath));

                    Commit commit = new Commit(getContentOfZipFile(getPathFolder("objects"), brancheCommitSha1));
                    String time = commit.getM_CreatedTime();
                    branchesList.add(new BranchDates(branchName, time));
                }
            }
            //todo: sort by date
            Collections.sort(branchesList, new Comparator<BranchDates>() {
                @Override
                public int compare(BranchDates o1, BranchDates o2) {
                    try {
                        return Integer.valueOf(dateFormat.parse(o1.getBranchLastCommitDate()).compareTo(dateFormat.parse(o2.getBranchLastCommitDate())));
                    } catch (ParseException e) {
                        e.printStackTrace();
                        return 0;
                    }
                }
            });
            int index =1;
            for (BranchDates branchDates : branchesList) {
                Path activeBranchPath = Paths.get(getPathFolder("branches") + File.separator + branchDates.getBranchName() + ".txt");

                if (!Files.exists(activeBranchPath)) {
                    //do nothing
                } else {

                    String brancheCommitSha1 = new String(Files.readAllBytes(activeBranchPath));
                    getAllCommits(brancheCommitSha1,commits,index);
                }
                index++;
            }
//            System.out.println(commits);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Collections.sort(commits, new Comparator<CommitNode>() {
            @Override
            public int compare(CommitNode o1, CommitNode o2) {
                try {
                    return Integer.valueOf(dateFormat.parse(o1.getTimestamp()).compareTo(dateFormat.parse(o2.getTimestamp())));
                } catch (ParseException e) {
                    e.printStackTrace();
                    return 0;
                }
            }
        });
        return commits;
    }

    private void getAllCommits(String Sha1, List<CommitNode> commits,int index) {
        Integer prev_branch_number = -1;
        Commit commit = new Commit(getContentOfZipFile(getPathFolder("objects"), Sha1));
        if (!listContainSha1(Sha1, commits, index)) {
            CommitNode commitNode = new CommitNode(commit.getM_CreatedTime(), commit.getM_CreatedBy(), commit.getM_Message(), index, -1, Sha1);
            commits.add(commitNode);
            if (!commit.getM_PreviousSHA1().equals("NONE")) {
                prev_branch_number = listContainPreviousSha1(commit.getM_PreviousSHA1(), commits);
                commits.get(commits.size() - 1).setPrevBranch(prev_branch_number);
                if (prev_branch_number == -1) {
                    getAllCommits(commit.getM_PreviousSHA1(), commits, index);
                }
            }
        }
    }

    private Integer listContainPreviousSha1(String m_previousSHA1, List<CommitNode> commits) {
        for(CommitNode commit: commits){
            if(commit.getSha1().equals(m_previousSHA1)) {
                return commit.getBranch_number();
            }
        }
        return -1;
    }

    private boolean listContainSha1(String sha1,List<CommitNode> commits,int index) {
        for(CommitNode commit: commits){
            if(commit.getSha1().equals(sha1)) {
                return true;
            }
        }
        return false;
    }

    public boolean isSha1Exist(String i_Sha1) {
        Path Sha1Path = Paths.get(getPathFolder("objects") + File.separator + i_Sha1 + ".zip");
        if (Files.exists(Sha1Path))
            return true;
        return false;
    }

    public void zeroingBranch(String i_Sha1) {
        String ActiveBranch = getBranchActiveName();
        updateBranchActiveCommit(i_Sha1);
        File rootFolder = new File(m_ActiveRepository + File.separator + getRootFolderName());
        deleteFolder(rootFolder);
        spreadCommitToWc(ActiveBranch);
    }

    public String getM_CollaborateWithPath() {
        return m_CollaborateWithPath;
    }

    public void setM_CollaborateWithPath(String m_CollaborateWithPath) {
        this.m_CollaborateWithPath = m_CollaborateWithPath;
    }


}
/* -------------------------------------------GENERAL  END------------------------------------- */

