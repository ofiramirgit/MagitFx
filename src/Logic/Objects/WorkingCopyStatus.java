package Logic.Objects;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class WorkingCopyStatus
{
    private Set<String> m_DeletedFilesList;
    private List<String> m_ChangedFilesList = new ArrayList<>();
    private List<String> m_NewFilesList = new ArrayList<>();
    private List<String> m_NotChangedList= new ArrayList<>();

    public Set<String> getM_DeletedFilesList() {
        return m_DeletedFilesList;
    }
    public List<String> getM_NotChangedFilesList() {
        return m_NotChangedList;
    }
    public void setM_DeletedFilesList(Set<String> m_DeletedFilesList) {
        this.m_DeletedFilesList = m_DeletedFilesList;
    }
    public void setM_NotChangedFilesList(Set<String> i_NotChangedList) {
        for(String str : i_NotChangedList)
        this.m_NotChangedList.add(str);
    }
    public List<String> getM_ChangedFilesList() {
        return m_ChangedFilesList;
    }
    public List<String> getM_NewFilesList() {
        return m_NewFilesList;
    }
    public Boolean isNotChanged()    {
        return (m_ChangedFilesList.size()==0 && m_NewFilesList.size()==0 && m_DeletedFilesList.isEmpty());
    }
    public Boolean IsEmpty()
    {
        return (m_DeletedFilesList.isEmpty() && m_ChangedFilesList.isEmpty() && m_NewFilesList.isEmpty());
    }

}