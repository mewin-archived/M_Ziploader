/*
 * Copyright (C) 2013 mewin<mewin001@hotmail.de>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.mewin.jhttp.mod.base;

import de.mewin.jhttp.JHTTP;
import de.mewin.jhttp.event.EventHandler;
import de.mewin.jhttp.event.Listener;
import de.mewin.jhttp.event.RequestDocumentEvent;
import de.mewin.jhttp.event.RequestDocumentEvent.HTTPAnswer;
import de.mewin.jhttp.http.StatusCode;
import de.mewin.jhttp.mod.Module;
import de.mewin.jhttp.util.HttpUtil;
import java.io.File;
import java.util.Stack;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 *
 * @author mewin<mewin001@hotmail.de>
 */
public class ZipLoaderModule extends Module implements Listener
{

    @Override
    protected void onEnable()
    {
        getServer().getEventManager().registerEvents(this);
    }
    
    @EventHandler
    public void onRequestDocument(RequestDocumentEvent e)
    {
        if (!e.getFile().exists())
        {
            File zip = findZip(e.getFile());
            if (zip != null)
            {
                String path = "";
                File notZip = e.getFile();
                while (!notZip.equals(zip))
                {
                    path = "/" + notZip.getName() + path;
                    notZip = notZip.getParentFile();
                }
                try
                {
                    ZipFile zipFile = new ZipFile(zip);
                    ZipEntry entry = zipFile.getEntry(path.substring(1)); //remove leading /
                    
                    if (entry != null && !entry.isDirectory())
                    {
                        HTTPAnswer ans = new RequestDocumentEvent.HTTPAnswer();
                        ans.header = HttpUtil.generateDefaultHeader(StatusCode.OK, "", JHTTP.getServer().getMimeType(path));
                        ans.in = zipFile.getInputStream(entry);
                        ans.header.addHeaderValue("Content-Length", String.valueOf(ans.in.available()));
                        e.setNewAnswer(ans);
                    }
                }
                catch(Exception ex)
                {
                    throw new RuntimeException(ex);
                }
            }
        }
    }
    
    private File findZip(File path)
    {
        if (!path.exists())
        {
            return findZip(path.getParentFile());
        }
        else
        {
            String name = path.getName().toLowerCase();
            
            if (name.endsWith(".zip")
                    || name.endsWith(".jar"))
            {
                return path;
            }
            else
            {
                return null;
            }
        }
    }
}