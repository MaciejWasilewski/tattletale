/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.tattletale.reporting;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.SortedSet;
import java.util.TreeSet;

import org.jboss.tattletale.core.Archive;
import org.jboss.tattletale.core.Location;
import org.jboss.tattletale.core.NestableArchive;

/**
 * Multiple locations report
 *
 * @author Jesper Pedersen <jesper.pedersen@jboss.org>
 * @author <a href="mailto:torben.jaeger@jit-consulting.de">Torben Jaeger</a>
 */
public class InvalidVersionReport extends AbstractReport
{
   /** NAME */
   private static final String NAME = "Invalid version";

   /** DIRECTORY */
   private static final String DIRECTORY = "invalidversion";

   /** Constructor */
   public InvalidVersionReport()
   {
      super(DIRECTORY, ReportSeverity.WARNING, NAME, DIRECTORY);
   }

   /**
    * write out the report's content
    * @param bw the writer to use
    * @throws IOException if an error occurs
    */
   public void writeHtmlBodyContent(BufferedWriter bw) throws IOException
   {
      bw.write("<table>" + Dump.newLine());

      bw.write("  <tr>" + Dump.newLine());
      bw.write("    <th>Name</th>" + Dump.newLine());
      bw.write("    <th>Location</th>" + Dump.newLine());
      bw.write("  </tr>" + Dump.newLine());

      boolean odd = true;

      for (Archive archive : archives)
      {
         String archiveName = archive.getName();
         int finalDot = archiveName.lastIndexOf('.');
         String extension = archiveName.substring(finalDot + 1);

         SortedSet<Location> locations = getLocations(archive);
         String version = locations.first().getVersion();

         if (null != version && !version.matches("\\d+(\\.\\d+(\\.\\d+(\\.[\\w\\-]+)?)?)?"))
         {
            boolean filtered = isFiltered(archiveName);

            if (!filtered)
            {
               status = ReportStatus.RED;
            }

            if (odd)
            {
               bw.write("  <tr class=\"rowodd\">" + Dump.newLine());
            }
            else
            {
               bw.write("  <tr class=\"roweven\">" + Dump.newLine());
            }
            bw.write("    <td><a href=\"../" + extension + "/" + archiveName + ".html\">" + archiveName
                  + "</a></td>" + Dump.newLine());

            bw.write("    <td>" + Dump.newLine());
            bw.write("      <table>" + Dump.newLine());

            for (Location location : locations)
            {
               bw.write("      <tr>" + Dump.newLine());

               bw.write("        <td>" + location.getFilename() + "</td>" + Dump.newLine());
               if (!filtered)
               {
                  bw.write("        <td>");
               }
               else
               {
                  bw.write("        <td style=\"text-decoration: line-through;\">");
               }
               if (null != location.getVersion())
               {
                  bw.write(location.getVersion());
               }
               else
               {
                  bw.write("<i>Not listed</i>");
               }
               bw.write("</td>" + Dump.newLine());

               bw.write("      </tr>" + Dump.newLine());
            }

            bw.write("      </table>" + Dump.newLine());

            bw.write("    </td>" + Dump.newLine());
            bw.write("  </tr>" + Dump.newLine());

            odd = !odd;
         }
      }
      bw.write("</table>" + Dump.newLine());
   }

   /**
    * Method getLocations.
    * @param archive Archive
    * @return SortedSet<Location>
    */
   private SortedSet<Location> getLocations(Archive archive)
   {
      final SortedSet<Location> locations = new TreeSet<Location>();
      if (archive instanceof NestableArchive)
      {
         final NestableArchive nestableArchive = (NestableArchive) archive;

         for (Archive sa : nestableArchive.getSubArchives())
         {
            locations.addAll(getLocations(sa));
         }
      }
      else
      {
         locations.addAll(archive.getLocations());
      }
      return locations;
   }

   /**
    * Create filter
    * @return The filter
    */
   @Override
   protected Filter createFilter()
   {
      return new KeyFilter();
   }
}
