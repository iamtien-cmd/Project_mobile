package vn.iostar.doan.activity;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent; // *** Thêm import Intent ***
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem; // *** Thêm import MenuItem ***
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull; // *** Thêm import NonNull ***
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar; // *** Thêm import Toolbar ***
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects; // *** Thêm import Objects ***

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.iostar.doan.R;
import vn.iostar.doan.adapter.CommentAdapter;
import vn.iostar.doan.api.ApiService; // Đảm bảo ApiService có định nghĩa các call cần thiết
import vn.iostar.doan.model.Comment;
import vn.iostar.doan.modelRequest.CommentRequest;
import vn.iostar.doan.model.Product; // Import nếu cần dùng Product
import vn.iostar.doan.model.User;   // Import nếu cần dùng User
import vn.iostar.doan.modelResponse.ImageUploadResponse;
import vn.iostar.doan.utils.SharedPreferencesUtils;

// *** Quan trọng: Import đúng lớp HomeActivity của bạn ***
import vn.iostar.doan.activity.HomeActivity; // Ví dụ, thay thế nếu tên/package khác


public class ProductRatingActivity extends AppCompatActivity {

    private RecyclerView recyclerViewComments;
    private CommentAdapter commentAdapter;
    private ProgressBar progressBar;
    private TextView tvNoComments;
    private Toolbar toolbar; // Biến Toolbar
    private ImageView ivAttachComment;
    private EditText etCommentInput;
    private MaterialButton btnSendComment;
    private ImageView ivSelectedImagePreview;
    private ImageView ivClearSelectedImage;
    private RatingBar ratingBar;

    private long productId;
    private long currentUserId = -1L;
    private Uri selectedImageUri = null;

    private ActivityResultLauncher<String> requestPermissionLauncher;
    private ActivityResultLauncher<PickVisualMediaRequest> pickMediaLauncher;

    private static final String TAG = "ProductRatingActivity";
    private static final String READ_STORAGE_PERMISSION = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
            ? Manifest.permission.READ_MEDIA_IMAGES
            : Manifest.permission.READ_EXTERNAL_STORAGE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActivityResultLaunchers(); // Khởi tạo launchers trước setContentView
        setContentView(R.layout.activity_product_rating); // Đảm bảo layout đúng

        // Lấy ID sản phẩm và người dùng
        productId = getIntent().getLongExtra("PRODUCT_ID", 1001); // Đặt giá trị mặc định là -1L để kiểm tra dễ hơn
        currentUserId = SharedPreferencesUtils.getLong(this, "userId", 1); // Đặt giá trị mặc định là -1L

        // Kiểm tra tính hợp lệ của ID
        if (productId == -1L || currentUserId <= 0) {
            Toast.makeText(this, "Invalid Product or User ID.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "Invalid IDs - Product: " + productId + ", User: " + currentUserId);
            finish(); // Kết thúc activity nếu ID không hợp lệ
            return;
        }
        Log.d(TAG, "Activity started for Product ID: " + productId + ", User ID: " + currentUserId);

        // Ánh xạ các View từ layout XML
        toolbar = findViewById(R.id.toolbar); // Đảm bảo ID này đúng trong XML
        recyclerViewComments = findViewById(R.id.recyclerViewComments);
        progressBar = findViewById(R.id.progressBar);
        tvNoComments = findViewById(R.id.tvNoComments);
        ivAttachComment = findViewById(R.id.ivAttachComment);
        etCommentInput = findViewById(R.id.etCommentInput);
        btnSendComment = findViewById(R.id.btnSendComment);
        ivSelectedImagePreview = findViewById(R.id.ivSelectedImagePreview);
        ivClearSelectedImage = findViewById(R.id.ivClearSelectedImage);
        ratingBar = findViewById(R.id.ratingBar);

        // Kiểm tra Null cho các View quan trọng (phòng trường hợp ID sai trong XML)
        if (toolbar == null || recyclerViewComments == null || progressBar == null || tvNoComments == null ||
                ivAttachComment == null || etCommentInput == null || btnSendComment == null ||
                ivSelectedImagePreview == null || ivClearSelectedImage == null || ratingBar == null) {
            Log.e(TAG, "CRITICAL ERROR: One or more essential views not found! Check XML IDs.");
            Toast.makeText(this, "UI Error. Cannot continue.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        Log.d(TAG, "All essential views found successfully.");

        // Thiết lập các thành phần UI và logic
        setupToolbar(); // Gọi hàm thiết lập Toolbar
        setupRecyclerView();
        setupClickListeners(); // Đặt listener SAU KHI đã ánh xạ view
        fetchComments(); // Tải comment ban đầu
        updateSendButtonState(); // Cập nhật trạng thái nút Gửi ban đầu
    }

    // Khởi tạo các ActivityResultLaunchers
    private void initActivityResultLaunchers() {
        // Launcher xin quyền đọc bộ nhớ
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        Log.d(TAG, "Storage permission granted.");
                        launchImagePicker(); // Nếu được cấp quyền thì mở bộ chọn ảnh
                    } else {
                        Log.w(TAG, "Storage permission denied.");
                        Toast.makeText(this, "Permission denied to access images.", Toast.LENGTH_SHORT).show();
                    }
                });

        // Launcher để chọn ảnh từ bộ nhớ
        pickMediaLauncher = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            if (uri != null) {
                Log.d(TAG, "Media selected: " + uri.toString());
                selectedImageUri = uri; // Lưu Uri ảnh đã chọn
                ivSelectedImagePreview.setImageURI(selectedImageUri); // Hiển thị ảnh preview
                ivSelectedImagePreview.setVisibility(View.VISIBLE);
                ivClearSelectedImage.setVisibility(View.VISIBLE); // Hiện nút xóa ảnh preview
            } else {
                Log.d(TAG, "No media selected by user.");
            }
        });
    }

    // Thiết lập Toolbar làm ActionBar
    private void setupToolbar() {
        setSupportActionBar(toolbar); // Đặt Toolbar làm ActionBar chính
        // Sử dụng Objects.requireNonNull để tránh cảnh báo NPE (hoặc kiểm tra null thủ công)
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true); // Hiển thị nút back (<-)
        getSupportActionBar().setTitle("Comment & Rating"); // Đặt tiêu đề cho Toolbar
    }

    // Thiết lập RecyclerView
    private void setupRecyclerView() {
        commentAdapter = new CommentAdapter(this);
        recyclerViewComments.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewComments.setAdapter(commentAdapter);
    }

    // Đặt các sự kiện click và thay đổi
    private void setupClickListeners() {
        ivAttachComment.setOnClickListener(v -> checkPermissionAndPickImage()); // Click nút đính kèm ảnh
        btnSendComment.setOnClickListener(v -> attemptSendComment()); // Click nút gửi comment
        ivClearSelectedImage.setOnClickListener(v -> clearSelectedImage()); // Click nút xóa ảnh đã chọn

        // Listener khi rating thay đổi bởi người dùng
        ratingBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            if (fromUser) {
                updateSendButtonState(); // Cập nhật trạng thái nút gửi
            }
        });

        // Listener khi text trong ô comment thay đổi
        etCommentInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                updateSendButtonState(); // Cập nhật trạng thái nút gửi
            }
        });
    }

    // Cập nhật trạng thái (enable/disable) của nút Gửi
    private void updateSendButtonState() {
        if (btnSendComment == null || ratingBar == null || etCommentInput == null) return; // Kiểm tra null

        boolean hasRating = ratingBar.getRating() > 0; // Đã rate chưa?
        boolean hasText = !etCommentInput.getText().toString().trim().isEmpty(); // Đã nhập text chưa?
        boolean isLoading = progressBar.getVisibility() == View.VISIBLE; // Có đang loading không?

        boolean shouldBeEnabled = hasRating && hasText && !isLoading; // Nút chỉ bật khi có cả rating, text và không loading

        // Chỉ thay đổi trạng thái nếu nó khác trạng thái hiện tại để tránh log thừa
        if (btnSendComment.isEnabled() != shouldBeEnabled) {
            Log.d(TAG, "Updating Send Button state. Should be enabled: " + shouldBeEnabled +
                    " (HasRating: " + hasRating + ", HasText: " + hasText + ", IsLoading: " + isLoading + ")");
            btnSendComment.setEnabled(shouldBeEnabled);
        }
    }

    // Kiểm tra quyền truy cập bộ nhớ và mở bộ chọn ảnh
    private void checkPermissionAndPickImage() {
        // Không cho chọn ảnh nếu đang gửi comment
        if (progressBar.getVisibility() == View.VISIBLE) {
            Toast.makeText(this, "Please wait...", Toast.LENGTH_SHORT).show();
            return;
        }
        // Kiểm tra quyền
        if (ContextCompat.checkSelfPermission(this, READ_STORAGE_PERMISSION) == PackageManager.PERMISSION_GRANTED) {
            launchImagePicker(); // Đã có quyền, mở bộ chọn
        } else {
            Log.d(TAG, "Requesting storage permission: " + READ_STORAGE_PERMISSION);
            requestPermissionLauncher.launch(READ_STORAGE_PERMISSION); // Chưa có quyền, yêu cầu
        }
    }

    // Mở ActivityResultLauncher để chọn ảnh
    private void launchImagePicker() {
        Log.d(TAG, "Launching media picker...");
        pickMediaLauncher.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE) // Chỉ chọn ảnh
                .build());
    }

    // Xóa ảnh đã chọn và ẩn preview
    private void clearSelectedImage() {
        selectedImageUri = null;
        ivSelectedImagePreview.setImageURI(null); // Xóa ảnh khỏi ImageView
        ivSelectedImagePreview.setVisibility(View.GONE); // Ẩn ImageView
        ivClearSelectedImage.setVisibility(View.GONE); // Ẩn nút xóa
        Log.d(TAG, "Cleared selected image.");
    }

    // Cố gắng gửi comment (sau khi người dùng nhấn nút Gửi)
    private void attemptSendComment() {
        // Kiểm tra null lần nữa cho chắc
        if (ratingBar == null || etCommentInput == null || btnSendComment == null) {
            Log.e(TAG,"AttemptSendComment: Critical views are null!");
            return;
        }

        int ratingValue = (int) ratingBar.getRating();
        String commentText = etCommentInput.getText().toString().trim();

        // Validate đầu vào
        if (commentText.isEmpty()) {
            etCommentInput.setError("Comment cannot be empty");
            etCommentInput.requestFocus();
            Log.w(TAG, "Send attempt failed: Comment text is empty.");
            return;
        }
        if (ratingValue == 0) {
            Toast.makeText(this, "Please provide a rating (at least 1 star)", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "Send attempt failed: Rating is zero.");
            return;
        }

        Log.d(TAG, "Comment validation passed. Proceeding to send...");
        showLoading(true); // Hiển thị loading và tắt các nút

        // Kiểm tra xem có ảnh được chọn không
        if (selectedImageUri != null) {
            Log.d(TAG, "Image selected, starting upload process...");
            // Nếu có ảnh, upload trước
            uploadImageToServer(selectedImageUri, new ImageUploadCallback() {
                @Override
                public void onSuccess(String uploadedImageUrl) {
                    // Upload thành công, gửi comment với URL ảnh
                    Log.i(TAG, "Image upload successful. Sending comment data with URL: " + uploadedImageUrl);
                    sendCommentData(commentText, ratingValue, uploadedImageUrl);
                }
                @Override
                public void onError(String error) {
                    // Upload thất bại
                    Log.e(TAG, "Image upload failed: " + error);
                    Toast.makeText(ProductRatingActivity.this, "Image upload failed: " + error, Toast.LENGTH_LONG).show();
                    showLoading(false); // Tắt loading và bật lại các nút (updateSendButtonState sẽ xử lý nút Send)
                }
            });
        } else {
            // Không có ảnh, gửi comment không cần URL ảnh
            Log.d(TAG, "No image selected. Sending comment data without image URL.");
            sendCommentData(commentText, ratingValue, null);
        }
    }

    // Interface callback cho việc upload ảnh
    private interface ImageUploadCallback {
        void onSuccess(String imageUrl);
        void onError(String error);
    }

    // Hàm thực hiện upload ảnh lên server
    private void uploadImageToServer(Uri imageUri, ImageUploadCallback callback) {
        Log.d(TAG, "Executing image upload for URI: " + imageUri);

        ContentResolver contentResolver = getContentResolver();
        String mimeType = contentResolver.getType(imageUri);
        // Đoán MIME type nếu không lấy được trực tiếp
        if (mimeType == null) {
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(imageUri.toString());
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension);
            mimeType = (mimeType == null) ? "image/jpeg" : mimeType; // Mặc định là jpeg
        }
        Log.d(TAG, "Using MIME type: " + mimeType);

        InputStream inputStream = null;
        try {
            inputStream = contentResolver.openInputStream(imageUri);
            if (inputStream == null) throw new IOException("Unable to open InputStream for URI: " + imageUri);

            // Đọc dữ liệu ảnh thành byte array
            byte[] fileBytes;
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] data = new byte[4096];
            int nRead;
            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
            fileBytes = buffer.toByteArray();
            buffer.close(); // Đóng buffer sau khi dùng xong

            // Tạo RequestBody cho file ảnh
            RequestBody requestFileBody = RequestBody.create(fileBytes, MediaType.parse(mimeType));

            // Lấy hoặc tạo tên file
            String fileName = getFileNameFromUri(imageUri);
            Log.d(TAG, "Resolved filename for upload: " + fileName);

            // Tạo MultipartBody.Part
            // <<< QUAN TRỌNG: Đảm bảo tên "file" khớp với tham số @Part("...") bên backend >>>
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", fileName, requestFileBody);

            Log.d(TAG, "Calling uploadImage API endpoint...");
            // Gọi API upload ảnh sử dụng instance từ ApiService
            ApiService.apiService.uploadImage(body)
                    .enqueue(new Callback<ImageUploadResponse>() {
                        @Override
                        public void onResponse(@NonNull Call<ImageUploadResponse> call, @NonNull Response<ImageUploadResponse> response) {
                            if (response.isSuccessful() && response.body() != null && response.body().getImageUrl() != null && !response.body().getImageUrl().isEmpty()) {
                                // Upload thành công
                                String uploadedImageUrl = response.body().getImageUrl();
                                Log.i(TAG, "API Image upload successful. URL: " + uploadedImageUrl);
                                callback.onSuccess(uploadedImageUrl); // Gọi callback thành công
                            } else {
                                // Lỗi từ server khi upload
                                String errorMsg = "Server error during upload: " + response.code() + " - " + response.message();
                                String responseBodyString = "";
                                try { if (response.errorBody() != null) responseBodyString = response.errorBody().string(); } catch (IOException e) { /* ignore */ }
                                Log.e(TAG, errorMsg + (!responseBodyString.isEmpty() ? "\nError Body: "+responseBodyString : ""));
                                callback.onError("Upload failed: " + response.code()); // Gọi callback lỗi
                            }
                        }
                        @Override
                        public void onFailure(@NonNull Call<ImageUploadResponse> call, @NonNull Throwable t) {
                            // Lỗi mạng khi upload
                            Log.e(TAG, "Upload network failure: " + t.getMessage(), t);
                            callback.onError("Network Error: " + t.getMessage()); // Gọi callback lỗi
                        }
                    });
        } catch (IOException e) {
            Log.e(TAG, "IOException processing image URI: " + e.getMessage(), e);
            callback.onError("Error reading image file");
        } catch (OutOfMemoryError e) {
            // Lỗi nếu ảnh quá lớn không đọc vào bộ nhớ được
            Log.e(TAG, "OutOfMemoryError reading image", e);
            callback.onError("Image file is too large");
        } finally {
            // Luôn đóng InputStream
            if (inputStream != null) {
                try { inputStream.close(); } catch (IOException e) { Log.e(TAG, "Error closing InputStream", e); }
            }
        }
    }

    // Hàm trợ giúp lấy tên file từ Uri (cải thiện để xử lý các trường hợp)
    private String getFileNameFromUri(Uri uri) {
        String result = null;
        if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
            try (Cursor cursor = getContentResolver().query(uri, new String[]{MediaStore.MediaColumns.DISPLAY_NAME}, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex);
                    }
                }
            } catch (Exception e) {
                Log.w(TAG, "Error getting filename from ContentResolver", e);
            }
        }
        if (result == null || result.isEmpty()) {
            result = uri.getLastPathSegment();
        }
        // Làm sạch tên file và tạo tên dự phòng nếu cần
        if (result != null) {
            result = result.replaceAll("[^a-zA-Z0-9.\\-]", "_"); // Thay các ký tự không hợp lệ
        }
        return (result != null && !result.isEmpty()) ? result : ("upload_" + System.currentTimeMillis());
    }


    // Gửi dữ liệu comment (text, rating, url ảnh nếu có) lên server
    private void sendCommentData(String text, int rating, String imageUrl) {
        Log.d(TAG, "Sending comment DTO - Text: " + text + ", Rating: " + rating + ", ImageUrl: " + imageUrl + ", UserID: " + currentUserId + ", ProductID: " + productId);

        // Tạo đối tượng Request Body
        CommentRequest requestBody = new CommentRequest(
                text,
                rating,
                imageUrl, // Có thể là null nếu không có ảnh
                productId,
                currentUserId
        );

        // Gọi API để tạo comment
        ApiService.apiService.createComment(requestBody)
                .enqueue(new Callback<Comment>() {
                    @Override
                    public void onResponse(@NonNull Call<Comment> call, @NonNull Response<Comment> response) {
                        // Tắt loading ngay cả khi API tạo comment bị lỗi (nhưng đã nhận response)
                        showLoading(false);

                        if (response.isSuccessful() && response.body() != null) {
                            // Tạo comment thành công
                            Log.i(TAG, "Comment created successfully! ID: " + response.body().getCommentId());
                            Toast.makeText(ProductRatingActivity.this, "Review submitted!", Toast.LENGTH_SHORT).show();
                            // Reset lại UI nhập liệu
                            if (etCommentInput != null) etCommentInput.setText("");
                            if (ratingBar != null) ratingBar.setRating(0);
                            clearSelectedImage();
                            fetchComments(); // Tải lại danh sách comment để hiển thị comment mới
                        } else {
                            // Lỗi từ API khi tạo comment
                            Log.e(TAG, "Failed to create comment API error: " + response.code() + " - " + response.message());
                            String errorBody = "";
                            try { if (response.errorBody() != null) errorBody = response.errorBody().string(); } catch (Exception e) { /* ignore */ }
                            Log.e(TAG, "Create Comment Error Body: " + errorBody);
                            Toast.makeText(ProductRatingActivity.this, "Failed to submit review: " + response.code(), Toast.LENGTH_LONG).show();
                            // Không cần bật lại nút ở đây, showLoading(false) đã gọi updateSendButtonState
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<Comment> call, @NonNull Throwable t) {
                        // Lỗi mạng khi gọi API tạo comment
                        Log.e(TAG, "Create comment NETWORK failure: " + t.getMessage(), t);
                        Toast.makeText(ProductRatingActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        showLoading(false); // Tắt loading và cập nhật lại trạng thái nút
                    }
                });
    }

    // Tải danh sách comment từ server
    private void fetchComments() {
        Log.d(TAG, "Fetching comments for product ID: " + productId);
        showLoading(true); // Bật loading trước khi gọi API

        ApiService.apiService.getCommentsByProduct(productId)
                .enqueue(new Callback<List<Comment>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<Comment>> call, @NonNull Response<List<Comment>> response) {
                        showLoading(false); // Tắt loading khi có kết quả (thành công hoặc lỗi)

                        if (response.isSuccessful() && response.body() != null) {
                            List<Comment> comments = response.body();
                            Log.d(TAG, "Successfully fetched " + comments.size() + " comments.");
                            if (comments.isEmpty()) {
                                // Hiển thị thông báo không có comment
                                if(tvNoComments != null) { tvNoComments.setText("Be the first to review!"); tvNoComments.setVisibility(View.VISIBLE); }
                                if (recyclerViewComments != null) recyclerViewComments.setVisibility(View.GONE);
                            } else {
                                // Hiển thị danh sách comment
                                if(tvNoComments != null) tvNoComments.setVisibility(View.GONE);
                                if (recyclerViewComments != null) recyclerViewComments.setVisibility(View.VISIBLE);
                                if (commentAdapter != null) commentAdapter.setComments(comments); // Cập nhật adapter
                            }
                        } else {
                            // Lỗi khi tải comment
                            Log.e(TAG, "Failed to load comments: " + response.code() + " - " + response.message());
                            if(tvNoComments != null) { tvNoComments.setText("Failed to load reviews."); tvNoComments.setVisibility(View.VISIBLE); }
                            if (recyclerViewComments != null) recyclerViewComments.setVisibility(View.GONE);
                            Toast.makeText(ProductRatingActivity.this, "Failed load: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                        // Luôn cập nhật trạng thái nút Send sau khi fetch xong
                        updateSendButtonState();
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<Comment>> call, @NonNull Throwable t) {
                        showLoading(false); // Tắt loading khi có lỗi mạng
                        Log.e(TAG, "Error fetching comments (Network): " + t.getMessage(), t);
                        if(tvNoComments != null) { tvNoComments.setText("Network error loading reviews."); tvNoComments.setVisibility(View.VISIBLE); }
                        if (recyclerViewComments != null) recyclerViewComments.setVisibility(View.GONE);
                        Toast.makeText(ProductRatingActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        updateSendButtonState(); // Cập nhật nút sau lỗi
                    }
                });
    }

    // Hiển thị/ẩn ProgressBar và bật/tắt các input liên quan
    private void showLoading(boolean isLoading) {
        Log.d(TAG, "Setting loading state to: " + isLoading);
        if (progressBar != null) progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);

        // Bật/tắt các input khác để người dùng không tương tác khi đang xử lý
        if (ivAttachComment != null) ivAttachComment.setEnabled(!isLoading);
        if (etCommentInput != null) etCommentInput.setEnabled(!isLoading);
        if (ratingBar != null) ratingBar.setEnabled(!isLoading);
        if (ivClearSelectedImage != null) ivClearSelectedImage.setEnabled(!isLoading); // Cũng tắt nút xóa ảnh khi loading

        // Xử lý nút Send riêng biệt thông qua updateSendButtonState
        updateSendButtonState(); // Gọi hàm này để nó tự quyết định dựa trên isLoading và input
    }


    // --- Xử lý nút Back trên Toolbar (Navigation Icon) ---
    @Override
    public boolean onSupportNavigateUp() {
        // Kiểm tra nếu đang loading thì không cho back
        if (progressBar.getVisibility() == View.VISIBLE) {
            Toast.makeText(this, "Please wait...", Toast.LENGTH_SHORT).show();
            return true; // Đã xử lý (bằng cách chặn lại)
        }

        // Tạo Intent để chuyển đến HomeActivity
        // *** Thay thế HomeActivity.class bằng tên lớp Activity chính xác của bạn ***
        Intent intent = new Intent(ProductRatingActivity.this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish(); // Đóng Activity hiện tại
        return true; // Báo rằng chúng ta đã xử lý sự kiện này
    }
    // ------------------------------------------------------


    // --- Xử lý nút Back vật lý ---
    @Override
    public void onBackPressed() {
        // Kiểm tra nếu đang loading thì không cho back
        if (progressBar.getVisibility() == View.VISIBLE) {
            Toast.makeText(this, "Please wait...", Toast.LENGTH_SHORT).show();
            // Không gọi super.onBackPressed() để chặn hành động back
        } else {
            // Nếu không loading, thực hiện hành động back mặc định (đóng Activity)
            super.onBackPressed();
            // Hoặc nếu muốn nút back vật lý cũng về Home:
            // Intent intent = new Intent(ProductRatingActivity.this, HomeActivity.class);
            // intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            // startActivity(intent);
            // finish();
        }
    }

    // --- Xử lý các Item khác trên Menu (nếu có) ---
    // Bỏ trống nếu không có menu item nào khác ngoài nút back
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Không cần xử lý android.R.id.home ở đây nữa
        return super.onOptionsItemSelected(item);
    }
}