package lombok.publish;

import java.io.IOException;
import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Delete;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;

public class PublishToBucket {
	private static final boolean DEBUG = false;
	
	private static final class AppException extends Exception {
		AppException(String msg) {
			super(msg);
		}
	}
	
	public static void main(String[] args) {
		try {
			if (args.length != 4) throw new AppException("4 args required: [path to creds file] [path to file root to upload] [target dir in bucket] [delete files to create a perfect copy or not]");
			boolean delete;
			if (args[3].equalsIgnoreCase("true")) delete = true;
			else if (args[3].equalsIgnoreCase("false")) delete = false;
			else throw new AppException("4th arg must be 'true' or 'false'");
			new PublishToBucket().go(args[0], args[1], args[2], delete);
			System.exit(0);
		} catch (AppException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}
	}
	
	private URI endpoint;
	private String bucketName;
	private AwsBasicCredentials creds;
	
	/**
	 * @param credsPath path to the creds file; first line in that file is the access key, second line is the secret.
	 * @param rootPath path to a directory; this directory is replicated into the bucket.
	 */
	private void go(String credsPath, String rootPath, String bucketDir, boolean delete) throws AppException {
		readCreds(Paths.get(credsPath));
		
		S3Client s3 = S3Client.builder()
			.endpointOverride(endpoint)
			.region(Region.of("auto"))
			.credentialsProvider(() -> creds)
			.build();
		
		ListObjectsV2Response objList = s3.listObjectsV2(ListObjectsV2Request.builder()
			.bucket(bucketName)
			.prefix(bucketDir + "/")
			.build());
		
		Set<String> inBucketBeforeUpload = new HashSet<String>();
		for (S3Object obj : objList.contents()) inBucketBeforeUpload.add(obj.key());
		
		dbg("Already in bucket:\n" +
			inBucketBeforeUpload.stream().map(x -> "  " + x + "\n").collect(Collectors.joining()) +
			(inBucketBeforeUpload.isEmpty() ? "  (Nothing)\n" : ""));
		
		try {
			go0(s3, inBucketBeforeUpload, bucketDir + "/", Paths.get(rootPath));
		} catch (IOException e) {
			throw new AppException("I/O exception uploading: " + e.getClass() + ": " + e.getMessage());
		}
		
		if (delete) {
			dbg("Uploads complete. Files to delete:\n" +
				inBucketBeforeUpload.stream().map(x -> "  " + x + "\n").collect(Collectors.joining()) +
				(inBucketBeforeUpload.isEmpty() ? "  (Nothing)\n" : ""));
			
			if (!inBucketBeforeUpload.isEmpty()) {
				List<ObjectIdentifier> objsToDelete = new ArrayList<ObjectIdentifier>();
				for (String key : inBucketBeforeUpload) {
					objsToDelete.add(ObjectIdentifier.builder().key(key).build());
				}
				s3.deleteObjects(DeleteObjectsRequest.builder()
					.bucket(bucketName)
					.delete(Delete.builder().objects(objsToDelete).build())
					.build());
				dbg("Deletion completed");
			}
		}
	}
	
	private static void dbg(String msg) {
		if (DEBUG) System.out.println(msg);
	}

	private void go0(S3Client s3, Set<String> inBucketBeforeUpload, String prefix, Path tgt) throws IOException {
		try (DirectoryStream<Path> ds = Files.newDirectoryStream(tgt)) {
			for (Path child : ds) {
				if (Files.isDirectory(child)) {
					go0(s3, inBucketBeforeUpload, prefix + child.getFileName().toString() + "/", child);
					continue;
				}
				
				String key = prefix + child.getFileName().toString();
				PutObjectRequest req = PutObjectRequest.builder()
					.bucket(bucketName)
					.key(key)
					.build();
				s3.putObject(req, child);
				boolean overwrote = inBucketBeforeUpload.remove(key);
				dbg("Uploaded: " + key + (overwrote ? " (overwrote)" : ""));
			}
		}
	}
	
	private static final String LINE_DESCRIPTIONS = "accessKey/secretKey/endpoint/bucket";
	private void readCreds(Path path) throws AppException {
		try {
			List<String> lines = Files.readAllLines(path);
			String accessKey = null, secretKey = null, endPoint = null, bucketName = null;
			for (String line : lines) {
				int idx = line.indexOf('#');
				if (idx != -1) line = line.substring(0, idx);
				line = line.trim();
				if (line.isEmpty()) continue;
				if (accessKey == null) { accessKey = line; continue; }
				if (secretKey == null) { secretKey = line; continue; }
				if (endPoint == null) { endPoint = line; continue; }
				if (bucketName == null) { bucketName = line; continue; }
				throw new AppException("Too many lines in " + path.toAbsolutePath() + " - only 4 expected: " + LINE_DESCRIPTIONS);
			}
			if (bucketName == null) throw new AppException("Expected 3 lines in " + path.toAbsolutePath() + ": " + LINE_DESCRIPTIONS);
			creds = AwsBasicCredentials.create(accessKey, secretKey);
			endpoint = URI.create(endPoint);
			this.bucketName = bucketName;
		} catch (NoSuchFileException e) {
			throw new AppException("File with bucket endpoint + credentials is not available. Make file " + path.toAbsolutePath() + "; it should contain something like: \n" +
				"123456789abcdef0123456789abcdef0  # this is the access key\n" +
				"123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef0  # this is the secret\n" +
				"https://12345.r2.cloudflarestorage.com   # this is the endpoint\n" +
				"lombok-data   # this is the bucket name");
		} catch (IOException e) {
			throw new AppException("I/O issue reading creds file " + path.toAbsolutePath() + ": " + e.getClass() + ": " + e.getMessage());
		}
	}
}
