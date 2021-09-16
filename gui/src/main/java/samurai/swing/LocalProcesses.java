/*
 * Copyright 2003-2015 Yusuke Yamamoto
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package samurai.swing;

import com.sun.tools.attach.AttachNotSupportedException;
import samurai.remotedump.ProcessUtil;
import samurai.remotedump.ThreadDumpUtil;
import samurai.remotedump.VM;
import samurai.util.Configuration;
import sun.jvmstat.monitor.MonitorException;

import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LocalProcesses {
    private final FileHistory fileHistory;
    private final JMenu localProcessesMenu;

    public LocalProcesses(Configuration config, FileHistory fileHistory, JMenu localProcessesMenu) {
        config.apply(this);
        this.fileHistory = fileHistory;
        this.localProcessesMenu = localProcessesMenu;
        localProcessesMenu.addMenuListener(new MenuListener() {
            @Override
            public void menuSelected(MenuEvent e) {
                updateChildMenuItems();
            }

            @Override
            public void menuDeselected(MenuEvent e) {

            }

            @Override
            public void menuCanceled(MenuEvent e) {

            }
        });
    }

    public JMenu getLocalProcessesMenu() {
        return this.localProcessesMenu;
    }

    private void updateChildMenuItems() {
        try {
            List<VM> currentVms = ProcessUtil.getVMs("localhost");

            for (int i = 0; i < localProcessesMenu.getItemCount(); i++) {
                LocalProcessMenuItem item = (LocalProcessMenuItem) localProcessesMenu.getItem(i);
                boolean found = false;
                for (VM vm : currentVms) {
                    if (item.getVm().getPid() == vm.getPid()) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    localProcessesMenu.remove(item);
                }

            }

            for (VM vm : currentVms) {
                boolean found = false;
                for (int i = 0; i < localProcessesMenu.getItemCount(); i++) {
                    LocalProcessMenuItem item = (LocalProcessMenuItem) localProcessesMenu.getItem(i);
                    if (item.getVm().getPid() == vm.getPid()) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    JMenuItem localProcess = new LocalProcessMenuItem(vm);
                    localProcess.setToolTipText(vm.getFullCommandLine());
                    localProcessesMenu.add(localProcess);
                }
            }
        } catch (URISyntaxException | MonitorException e) {
            e.printStackTrace();
        }
    }

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");

    ExecutorService executor = Executors.newFixedThreadPool(1);

    class LocalProcessMenuItem extends JMenuItem {
        private static final long serialVersionUID = 2629440395264682598L;
        final VM vm;

        public LocalProcessMenuItem(VM vm) {
            super(String.format("%s %s", vm.getPid(), vm.getFqcn()));
            this.vm = vm;
            addActionListener(e -> executor.execute(() -> {
                try {
                    Path path = Paths.get(System.getProperty("user.home"),
                            String.format("%s-%d-%s.txt", vm.getFqcn(), vm.getPid(), LocalDateTime.now().format(dateTimeFormatter)));
                    Files.writeString(path, LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "\n", StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                    Files.writeString(path, "pid: " + vm.getPid() + "\n\n", StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                    Files.writeString(path, "FQCN: " + vm.getFqcn() + "\n\n", StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                    Files.writeString(path, String.format("Command line:\n%s\n\n", vm.getFullCommandLine()), StandardOpenOption.CREATE, StandardOpenOption.APPEND);

                    SwingUtilities.invokeLater(() -> fileHistory.open(path.toFile()));

                    for (int i = 0; i < 3; i++) {
                        Files.write(path, ThreadDumpUtil.getThreadDump(vm.getPid()), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                        Thread.sleep(1000);
                    }
                } catch (InterruptedException | AttachNotSupportedException | IOException e1) {
                    e1.printStackTrace();
                }
            }));
        }

        public VM getVm() {
            return vm;
        }

    }
}
