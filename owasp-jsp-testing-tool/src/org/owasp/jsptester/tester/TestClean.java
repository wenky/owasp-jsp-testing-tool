/*
 * Copyright (c) 2008, Jason Li
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * (1) Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer; (2) Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution; (3) Neither the name of OWASP nor
 * the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package org.owasp.jsptester.tester;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.owasp.jsptester.conf.Configuration;

/**
 * @author Jason Li
 *
 */
public class TestClean
{

    private static void deleteFilesInDir(File directory)
    {
        File[] files = directory.listFiles();
        for (int fileIdx = 0; fileIdx<files.length; fileIdx++){
            File toDelete = files[fileIdx];
            if (toDelete.exists()) {
                if (toDelete.isDirectory()){
                    deleteFilesInDir( toDelete );
                }
                
                toDelete.delete();
            }
        }
    }
    
    /**
     * @param args
     */
    public static void main( String[] args ) throws IOException
    {
        File dir = new File(Configuration.getInstance().getProperty( Configuration.EMBEDDED_DOC_BASE ));
        FileUtils.cleanDirectory( dir );
//        deleteFilesInDir(dir);
        dir.delete();
    }

}
