package com.revenga.rits.smartscheduler.lib.util;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import lombok.extern.log4j.Log4j2;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;

@Log4j2
public class SFTPUtil {

	private SFTPUtil() {

		throw new IllegalStateException(this.getClass().getSimpleName());
	}

	public static File getFile(File file, String user, String password, String host, Integer port, String localTempPath,
			Integer maxAttempts, Long waitTimeInMilliseconds) {

		File localTempFile = null;

		if (file != null && localTempPath != null) {

			Pair<SSHClient, SFTPClient> pair = getClient(user, password, host, port, maxAttempts,
					waitTimeInMilliseconds);

			if (pair != null) {

				try {
					String pathName = ResourcesUtil.getPath(localTempPath);

					File localDirectory = new File(pathName);

					if (!localDirectory.exists()) {

						localDirectory.mkdirs();
					}

					pathName = pathName + "/" + file.getName();
					localTempFile = new File(pathName);
					
					if (!localTempFile.exists()) {
						
						pair.getRight().get(file.getPath(), pathName);

					} else {
						
						localTempFile = null;
						log.warn("The file "  + pathName + " has already been downloaded");
					}

				} catch (IOException e) {

					log.error(e.getMessage());
					log.debug(ExceptionUtils.getStackTrace(e));
					localTempFile = null;

				} finally {

					try {
						pair.getRight().close();

					} catch (IOException e) {

						log.error(e.getMessage());
					}

					try {
						pair.getLeft().disconnect();

					} catch (IOException e) {

						log.error(e.getMessage());
					}

				}
			}
		} else {
			log.error("SFTPUtil:getFile Null value");
		}

		return localTempFile;
	}

	public static boolean putFile(File localFile, File remoteFile, String user, String password, String host,
			Integer port, String localTempPath, Integer maxAttempts, Long waitTimeInMilliseconds) {

		boolean resp = false;

		if (localFile != null && remoteFile != null && localTempPath != null) {

			Pair<SSHClient, SFTPClient> pair = getClient(user, password, host, port, maxAttempts,
					waitTimeInMilliseconds);

			if (pair != null) {

				try {
					pair.getRight().put(localFile.getPath(), remoteFile.getPath());
					resp = true;

				} catch (IOException e) {

					log.error(e.getMessage());
					log.debug(e.getStackTrace());

				} finally {

					try {
						pair.getRight().close();

					} catch (IOException e) {

						log.error(e.getMessage());
					}

					try {
						pair.getLeft().disconnect();

					} catch (IOException e) {

						log.error(e.getMessage());
					}

				}
			}

		} else {
			log.error("SFTPUtil:putFile Null value");
		}
		return resp;

	}

	private static Pair<SSHClient, SFTPClient> getClient(String user, String password, String host, Integer port,
			Integer maxAttempts, Long waitTimeInMilliseconds) {

		Pair<SSHClient, SFTPClient> pair = null;

		if (maxAttempts != null && waitTimeInMilliseconds != null && user != null && password != null && host != null
				&& port != null) {

			SSHClient sshClient = initSshj(user, password, host, port);
			SFTPClient sftpClient = null;

			try {

				if (sshClient != null) {

					sftpClient = sshClient.newSFTPClient();

				} else {

					int attemps = 1;

					while (sshClient == null && attemps <= maxAttempts) {

						log.error("SFTPUtil:getClient Cannot connect to SFTP, traying again: Attemp: " + attemps
								+ " Of " + maxAttempts);

						Thread.sleep(waitTimeInMilliseconds);

						sshClient = initSshj(user, password, host, port);

						if (sshClient != null) {

							sftpClient = sshClient.newSFTPClient();
						}

						attemps++;
					}
				}

			} catch (IOException e) {

				try {

					sshClient.disconnect();

				} catch (IOException e1) {

					log.error(e.getMessage());
					log.debug(e.getStackTrace());
				}

			} catch (InterruptedException e1) {

				Thread.currentThread().interrupt();
			}

			if (sftpClient != null) {

				pair = new MutablePair<>(sshClient, sftpClient);
			}

		} else {

			log.error("SFTPUtil:getClient Null pointer in SFTP some config params. Check configuration");
		}

		return pair;
	}

	private static SSHClient initSshj(String user, String password, String host, Integer port) {

		SSHClient sshClient = null;

		try {
			sshClient = new SSHClient();
			sshClient.addHostKeyVerifier(new PromiscuousVerifier());
			sshClient.setConnectTimeout(2000);
			sshClient.setTimeout(2000);
			sshClient.connect(host, port);

			sshClient.useCompression();
			sshClient.authPassword(user, password);

		} catch (IOException e) {

			log.error(e.getMessage());
			log.debug(e.getStackTrace());

			try {
				sshClient.disconnect();

			} catch (IOException e1) {

				log.error(e.getMessage());
				log.debug(e.getStackTrace());

			} finally {
				sshClient = null;
			}
		}

		return sshClient;
	}

}
