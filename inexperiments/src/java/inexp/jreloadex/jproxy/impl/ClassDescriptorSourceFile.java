package inexp.jreloadex.jproxy.impl;

import java.io.File;
import java.util.LinkedList;

/**
 *
 * @author jmarranz
 */
public class ClassDescriptorSourceFile extends ClassDescriptor
{
    protected long timestamp;
    protected File sourceFile; 
    protected LinkedList<ClassDescriptor> innerClasses;
    
    public ClassDescriptorSourceFile(String className,File sourceFile, long timestamp) 
    {
        super(className,false);
        this.sourceFile = sourceFile;
        this.timestamp = timestamp;
    }

    public File getSourceFile()
    {
        return sourceFile;
    }
    
    public long getTimestamp() 
    {
        return timestamp;
    }

    public void updateTimestamp(long timestamp)
    {
        long oldTimestamp = this.timestamp;
        this.timestamp = timestamp;
        if (oldTimestamp != timestamp)
        {
            cleanSourceCodeChanged();
        }
    }

    public void cleanSourceCodeChanged()
    {
        // Como ha cambiado la clase, reseteamos las dependencias        
        setClassBytes(null);
        setLastLoadedClass(null);
        clearInnerClassDescriptors(); // El código fuente nuevo puede haber cambiado totalmente las innerclasses antiguas (añadido, eliminado)
    }
    
    public boolean isInnerClass(String className)
    {
        int pos = className.lastIndexOf('$');
        if (pos == -1)
            return false; // No es innerclass
        String baseClassName = className.substring(0, pos);
        return this.className.equals(baseClassName); // Si es false es que es una innerclass pero de otra clase
    }
    
    public LinkedList<ClassDescriptor> getInnerClassDescriptors()
    {
        return innerClasses;
    }
    
    public void clearInnerClassDescriptors()
    {
        if (innerClasses != null)
            innerClasses.clear();       
    }
    
    public ClassDescriptor getInnerClassDescriptor(String className,boolean addWhenMissing)
    {
        if (innerClasses != null)
        {
            for(ClassDescriptor classDesc : innerClasses)
            {
                if (classDesc.getClassName().equals(className))
                    return classDesc;
            }
        }
        
        if (!addWhenMissing) return null;
        
        return addInnerClassDescriptor(className);
    }
        
    public ClassDescriptor addInnerClassDescriptor(String className)
    {
        if (!isInnerClass(className))
            return null;
        
        if (innerClasses == null)
            innerClasses = new LinkedList<ClassDescriptor>();
        
        ClassDescriptor classDesc = new ClassDescriptor(className,true);
        innerClasses.add(classDesc);
        return classDesc;
    }    
    
    @Override
    public void resetLastLoadedClass()
    {
        super.resetLastLoadedClass();

        LinkedList<ClassDescriptor> innerClassDescList = getInnerClassDescriptors();
        if (innerClassDescList != null)
        {
            for(ClassDescriptor innerClassDesc : innerClassDescList)
                innerClassDesc.resetLastLoadedClass();             
        }   
    }
    
    public static String getClassNameFromSourceFileAbsPath(String path,String rootPathOfSources)
    {
        // path y rootPathOfSources son absolutos, preferentemente obtenidos con File.getAbsolutePath()
        int pos = path.indexOf(rootPathOfSources); 
        if (pos != 0) // DEBE SER 0, NO debería ocurrir
            return null;
        path = path.substring(rootPathOfSources.length() + 1); // Sumamos +1 para quitar también el / separador del pathInput y el path relativo de la clase
        // Quitamos la extensión (.java)
        pos = path.lastIndexOf('.');        
        if (pos == -1) return null; // NO debe ocurrir
        path = path.substring(0, pos);        
        path = path.replace(File.separatorChar, '.');  // getAbsolutePath() normaliza con el caracter de la plataforma
        return path;
    }      
}
