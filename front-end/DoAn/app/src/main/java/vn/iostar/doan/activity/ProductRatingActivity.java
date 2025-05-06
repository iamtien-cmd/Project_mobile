package vn.iostar.doan.activity;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout; // Import này có thể cần nếu bạn dùng LinearLayout làm container
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList; // Import lại nếu chưa có
import java.util.List;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.iostar.doan.R;
import vn.iostar.doan.adapter.CommentAdapter;
import vn.iostar.doan.api.ApiService;
import vn.iostar.doan.model.Comment;
import vn.iostar.doan.modelRequest.CommentRequest;
import vn.iostar.doan.modelResponse.ImageUploadResponse;
import vn.iostar.doan.utils.SharedPreferencesUtils;

// *** QUAN TRỌNG: Thay thế bằng import HomeActivity thực tế của bạn ***
// import vn.iostar.doan.activity.HomeActivity;


public class ProductRatingActivity extends AppCompatActivity {

    private RecyclerView recyclerViewComments;
    private CommentAdapter commentAdapter;
    private ProgressBar progressBar;
    private TextView tvNoComments;
    private Toolbar toolbar;
    private ImageView ivAttachComment;
    private EditText etCommentInput;
    private MaterialButton btnSendComment;
    private ImageView ivSelectedImagePreview;
    private ImageView ivClearSelectedImage;
    private RatingBar ratingBar;
    // Tùy chọn: Nếu bạn nhóm các view nhập liệu vào một container (vd: LinearLayout) trong XML
    // private LinearLayout layoutRatingInputSection;

    private long productId;
    private long currentUserId = 0L; // Khởi tạo mặc định là 0
    private Uri selectedImageUri = null;

    private ActivityResultLauncher<String> requestPermissionLauncher;
    private ActivityResultLauncher<PickVisualMediaRequest> pickMediaLauncher;

    private static final String TAG = "ProductRatingActivity";
    private static final String READ_STORAGE_PERMISSION = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
            ? Manifest.permission.READ_MEDIA_IMAGES
            : Manifest.permission.READ_EXTERNAL_STORAGE;

    // Biến để theo dõi các cuộc gọi mạng đang diễn ra (Tùy chọn, nhưng hữu ích)
    private Call<ImageUploadResponse> imageUploadCall;
    private Call<Comment> createCommentCall;
    private Call<List<Comment>> fetchCommentsCall;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActivityResultLaunchers(); // Khởi tạo launchers trước
        setContentView(R.layout.activity_product_rating); // Đảm bảo layout đúng

        // 1. Lấy ID sản phẩm và người dùng
        productId = getIntent().getLongExtra("product_id", -1L);
        // Lấy userId, mặc định là 0 nếu không tìm thấy hoặc có lỗi
        currentUserId = SharedPreferencesUtils.getLong(this, "userId", 0L);

        // 2. Kiểm tra tính hợp lệ của Product ID (BẮT BUỘC)
        if (productId == -1L) {
            Toast.makeText(this, "Invalid Product ID.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "Invalid Product ID: " + productId);
            finish(); // Kết thúc activity nếu Product ID không hợp lệ
            return;
        }
        Log.i(TAG, "Activity started for Product ID: " + productId + ", User ID: " + currentUserId);

        // 3. Ánh xạ các View từ layout XML
        toolbar = findViewById(R.id.toolbar);
        recyclerViewComments = findViewById(R.id.recyclerViewComments);
        progressBar = findViewById(R.id.progressBar);
        tvNoComments = findViewById(R.id.tvNoComments);
        ivAttachComment = findViewById(R.id.ivAttachComment);
        etCommentInput = findViewById(R.id.etCommentInput);
        btnSendComment = findViewById(R.id.btnSendComment);
        ivSelectedImagePreview = findViewById(R.id.ivSelectedImagePreview);
        ivClearSelectedImage = findViewById(R.id.ivClearSelectedImage);
        ratingBar = findViewById(R.id.ratingBar);
        // Tùy chọn: Ánh xạ container
        // layoutRatingInputSection = findViewById(R.id.layoutRatingInputSection); // ID của container trong XML

        // 4. Kiểm tra Null cho các View quan trọng (phòng trường hợp ID sai trong XML)
        if (toolbar == null || recyclerViewComments == null || progressBar == null || tvNoComments == null ||
                ivAttachComment == null || etCommentInput == null || btnSendComment == null ||
                ivSelectedImagePreview == null || ivClearSelectedImage == null || ratingBar == null
            /* || layoutRatingInputSection == null */ ) { // Thêm container nếu dùng
            Log.e(TAG, "CRITICAL ERROR: One or more essential views not found! Check XML IDs.");
            Toast.makeText(this, "UI Error. Cannot continue.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        Log.d(TAG, "All essential views found successfully.");

        // 5. Thiết lập các thành phần UI và logic
        setupToolbar();
        setupRecyclerView();

        // 6. XỬ LÝ HIỂN THỊ PHẦN NHẬP LIỆU DỰA TRÊN USER ID
        if (currentUserId <= 0) {
            // Người dùng chưa đăng nhập (hoặc ID không hợp lệ) -> Ẩn phần nhập liệu
            Log.i(TAG, "User not logged in (User ID: " + currentUserId + "). Hiding rating/comment input section.");
            hideRatingInputSection();
        } else {
            // Người dùng đã đăng nhập -> Hiển thị phần nhập liệu và cài đặt listener
            Log.i(TAG, "User logged in (User ID: " + currentUserId + "). Showing rating/comment input section.");
            showRatingInputSection(); // Đảm bảo hiển thị (quan trọng nếu activity tái tạo)
            setupClickListeners();    // Chỉ cài đặt listener nếu người dùng có thể tương tác
            updateSendButtonState();  // Cập nhật trạng thái nút gửi ban đầu
        }

        // 7. Tải comment ban đầu (luôn tải, bất kể đăng nhập hay chưa)
        fetchComments();
    }

    // Hàm ẩn phần nhập liệu đánh giá
    private void hideRatingInputSection() {
        // Cách 1: Ẩn từng View (an toàn nếu không chắc về container)
        if (ratingBar != null) ratingBar.setVisibility(View.GONE);
        if (etCommentInput != null) etCommentInput.setVisibility(View.GONE);
        if (ivAttachComment != null) ivAttachComment.setVisibility(View.GONE);
        if (btnSendComment != null) btnSendComment.setVisibility(View.GONE);
        // Cũng ẩn preview ảnh nếu nó đang hiển thị
        if (ivSelectedImagePreview != null) ivSelectedImagePreview.setVisibility(View.GONE);
        if (ivClearSelectedImage != null) ivClearSelectedImage.setVisibility(View.GONE);

        // Cách 2: Ẩn cả container (nếu bạn đã nhóm chúng trong XML)
        // if (layoutRatingInputSection != null) {
        //     layoutRatingInputSection.setVisibility(View.GONE);
        // }
    }

    // Hàm hiển thị phần nhập liệu đánh giá (để đảm bảo nó hiện khi cần)
    private void showRatingInputSection() {
        // Cách 1: Hiện từng View
        if (ratingBar != null) ratingBar.setVisibility(View.VISIBLE);
        if (etCommentInput != null) etCommentInput.setVisibility(View.VISIBLE);
        if (ivAttachComment != null) ivAttachComment.setVisibility(View.VISIBLE);
        if (btnSendComment != null) btnSendComment.setVisibility(View.VISIBLE);
        // Không cần set VISIBLE cho ivSelectedImagePreview và ivClearSelectedImage ở đây
        // Chúng sẽ tự hiển thị khi ảnh được chọn
    }

    // Khởi tạo các ActivityResultLaunchers
    private void initActivityResultLaunchers() {
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

        pickMediaLauncher = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            if (uri != null) {
                Log.d(TAG, "Media selected: " + uri.toString());
                selectedImageUri = uri; // Lưu Uri ảnh đã chọn
                // Chỉ hiển thị preview nếu các nút input đang hiển thị và view không null
                if (ivSelectedImagePreview != null && ivClearSelectedImage != null && ratingBar != null && ratingBar.getVisibility() == View.VISIBLE) {
                    ivSelectedImagePreview.setImageURI(selectedImageUri); // Hiển thị ảnh preview
                    ivSelectedImagePreview.setVisibility(View.VISIBLE);
                    ivClearSelectedImage.setVisibility(View.VISIBLE); // Hiện nút xóa ảnh preview
                } else {
                    Log.w(TAG, "Image picked, but input section is hidden or views are null. Preview not shown.");
                }
            } else {
                Log.d(TAG, "No media selected by user.");
            }
        });
    }

    // Thiết lập Toolbar làm ActionBar
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) { // Kiểm tra null trước khi dùng
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Hiển thị nút back (<-)
            getSupportActionBar().setTitle("Comment & Rating"); // Đặt tiêu đề cho Toolbar
        } else {
            Log.e(TAG, "SupportActionBar is null after setting toolbar!");
        }
    }

    // Thiết lập RecyclerView
    private void setupRecyclerView() {
        commentAdapter = new CommentAdapter(this);
        recyclerViewComments.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewComments.setAdapter(commentAdapter);
    }

    // Đặt các sự kiện click và thay đổi - **CHỈ GỌI NẾU USER ĐÃ ĐĂNG NHẬP**
    private void setupClickListeners() {
        // Gán listener chỉ khi các view tương ứng không null
        if (ivAttachComment != null) {
            ivAttachComment.setOnClickListener(v -> checkPermissionAndPickImage());
        }
        if (btnSendComment != null) {
            btnSendComment.setOnClickListener(v -> attemptSendComment());
        }
        if (ivClearSelectedImage != null) {
            ivClearSelectedImage.setOnClickListener(v -> clearSelectedImage());
        }

        if (ratingBar != null) {
            ratingBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
                if (fromUser) {
                    updateSendButtonState();
                }
            });
        }

        if (etCommentInput != null) {
            etCommentInput.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override public void afterTextChanged(Editable s) {
                    updateSendButtonState();
                }
            });
        }
    }

    // Cập nhật trạng thái (enable/disable) của nút Gửi
    private void updateSendButtonState() {
        // --- SỬA ĐỔI: Thêm kiểm tra null rõ ràng hơn ngay trước khi sử dụng ---
        if (btnSendComment == null || ratingBar == null) {
            Log.w(TAG, "updateSendButtonState: Button or RatingBar is null. Cannot update state.");
            return; // Thoát sớm nếu các view cơ bản bị null
        }

        // Kiểm tra xem phần nhập liệu có ẩn không
        if (ratingBar.getVisibility() == View.GONE) {
            btnSendComment.setEnabled(false); // Nếu ẩn thì luôn tắt nút gửi
            return;
        }

        // Kiểm tra etCommentInput TRƯỚC khi dùng getText()
        if (etCommentInput == null) {
            Log.e(TAG, "updateSendButtonState: etCommentInput is null unexpectedly!");
            btnSendComment.setEnabled(false); // Lỗi nghiêm trọng, tắt nút
            return;
        }

        boolean hasRating = ratingBar.getRating() > 0;
        boolean hasText = !etCommentInput.getText().toString().trim().isEmpty(); // Bây giờ an toàn để gọi getText
        boolean isLoading = progressBar != null && progressBar.getVisibility() == View.VISIBLE; // Kiểm tra progressBar null

        // Nút chỉ bật khi có cả rating, text và không loading VÀ phần input đang hiển thị (đã kiểm tra ở trên)
        boolean shouldBeEnabled = hasRating && hasText && !isLoading;

        // Chỉ thay đổi trạng thái nếu nó khác trạng thái hiện tại để tránh log thừa
        if (btnSendComment.isEnabled() != shouldBeEnabled) {
            Log.d(TAG, "Updating Send Button state. Should be enabled: " + shouldBeEnabled +
                    " (HasRating: " + hasRating + ", HasText: " + hasText + ", IsLoading: " + isLoading + ")");
            btnSendComment.setEnabled(shouldBeEnabled);
        }
    }

    // Kiểm tra quyền truy cập bộ nhớ và mở bộ chọn ảnh
    private void checkPermissionAndPickImage() {
        // Kiểm tra progressBar null trước khi truy cập visibility
        if (progressBar != null && progressBar.getVisibility() == View.VISIBLE) {
            Toast.makeText(this, "Please wait...", Toast.LENGTH_SHORT).show();
            return;
        }
        // Kiểm tra quyền
        if (ContextCompat.checkSelfPermission(this, READ_STORAGE_PERMISSION) == PackageManager.PERMISSION_GRANTED) {
            launchImagePicker(); // Đã có quyền, mở bộ chọn
        } else {
            Log.d(TAG, "Requesting storage permission: " + READ_STORAGE_PERMISSION);
            if (requestPermissionLauncher != null) { // Kiểm tra launcher không null
                requestPermissionLauncher.launch(READ_STORAGE_PERMISSION); // Chưa có quyền, yêu cầu
            } else {
                Log.e(TAG, "requestPermissionLauncher is null! Cannot request permission.");
            }
        }
    }

    // Mở ActivityResultLauncher để chọn ảnh
    private void launchImagePicker() {
        Log.d(TAG, "Launching media picker...");
        if (pickMediaLauncher != null) { // Kiểm tra launcher không null
            pickMediaLauncher.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE) // Chỉ chọn ảnh
                    .build());
        } else {
            Log.e(TAG, "pickMediaLauncher is null! Cannot launch picker.");
        }
    }

    // Xóa ảnh đã chọn và ẩn preview
    private void clearSelectedImage() {
        selectedImageUri = null;
        if (ivSelectedImagePreview != null) {
            ivSelectedImagePreview.setImageURI(null); // Xóa ảnh khỏi ImageView
            ivSelectedImagePreview.setVisibility(View.GONE); // Ẩn ImageView
        }
        if (ivClearSelectedImage != null) {
            ivClearSelectedImage.setVisibility(View.GONE); // Ẩn nút xóa
        }
        Log.d(TAG, "Cleared selected image.");
    }

    // Cố gắng gửi comment (sau khi người dùng nhấn nút Gửi)
    private void attemptSendComment() {
        // Kiểm tra lại userId lần nữa cho chắc chắn
        if (currentUserId <= 0) {
            Log.e(TAG, "AttemptSendComment called but user is not logged in (ID=" + currentUserId + "). This should not happen if UI is hidden correctly.");
            return;
        }

        // Kiểm tra null cho các View cần thiết
        if (ratingBar == null || etCommentInput == null || btnSendComment == null) {
            Log.e(TAG,"AttemptSendComment: Critical views for input are null!");
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
            uploadImageToServer(selectedImageUri, new ImageUploadCallback() {
                @Override
                public void onSuccess(String uploadedImageUrl) {
                    // --- SỬA ĐỔI: Kiểm tra Activity còn hợp lệ trước khi gửi comment ---
                    if (isFinishing() || isDestroyed()) {
                        Log.w(TAG, "uploadImageToServer onSuccess: Activity is finishing or destroyed. Aborting sendCommentData.");
                        showLoading(false); // Tắt loading nếu có lỗi
                        return;
                    }
                    Log.i(TAG, "Image upload successful. Sending comment data with URL: " + uploadedImageUrl);
                    sendCommentData(commentText, ratingValue, uploadedImageUrl);
                }
                @Override
                public void onError(String error) {
                    // --- SỬA ĐỔI: Kiểm tra Activity còn hợp lệ trước khi hiển thị Toast ---
                    if (isFinishing() || isDestroyed()) {
                        Log.w(TAG, "uploadImageToServer onError: Activity is finishing or destroyed. Ignoring error display.");
                        return;
                    }
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
        if (imageUri == null) {
            callback.onError("Image URI is null");
            return;
        }

        ContentResolver contentResolver = getContentResolver();
        String mimeType = contentResolver.getType(imageUri);
        if (mimeType == null) {
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(imageUri.toString());
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension);
            mimeType = (mimeType == null) ? "image/jpeg" : mimeType; // Default to jpeg
        }
        Log.d(TAG, "Using MIME type: " + mimeType);

        InputStream inputStream = null;
        try {
            inputStream = contentResolver.openInputStream(imageUri);
            if (inputStream == null) throw new IOException("Unable to open InputStream for URI: " + imageUri);

            // --- SỬA ĐỔI: Sử dụng try-with-resources cho ByteArrayOutputStream ---
            byte[] fileBytes;
            try (ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
                byte[] data = new byte[4096];
                int nRead;
                while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, nRead);
                }
                buffer.flush();
                fileBytes = buffer.toByteArray();
            } // buffer.close() sẽ tự động được gọi

            RequestBody requestFileBody = RequestBody.create(fileBytes, MediaType.parse(mimeType));
            String fileName = getFileNameFromUri(imageUri);
            Log.d(TAG, "Resolved filename for upload: " + fileName);

            // *** Đảm bảo tên "file" khớp với backend API ***
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", fileName, requestFileBody);

            Log.d(TAG, "Calling uploadImage API endpoint...");
            // Hủy call cũ nếu có
            cancelCall(imageUploadCall);
            imageUploadCall = ApiService.apiService.uploadImage(body); // Gán call mới

            imageUploadCall.enqueue(new Callback<ImageUploadResponse>() { // Đảm bảo ImageUploadResponse đúng
                @Override
                public void onResponse(@NonNull Call<ImageUploadResponse> call, @NonNull Response<ImageUploadResponse> response) {
                    // --- SỬA ĐỔI: Kiểm tra Activity hợp lệ ---
                    if (isFinishing() || isDestroyed()) {
                        Log.w(TAG, "uploadImageToServer onResponse: Activity is finishing or destroyed. Ignoring callback.");
                        return;
                    }

                    if (response.isSuccessful() && response.body() != null && response.body().getImageUrl() != null && !response.body().getImageUrl().isEmpty()) {
                        String uploadedImageUrl = response.body().getImageUrl();
                        Log.i(TAG, "API Image upload successful. URL: " + uploadedImageUrl);
                        callback.onSuccess(uploadedImageUrl);
                    } else {
                        String errorMsg = "Server error during upload: " + response.code() + " - " + response.message();
                        String responseBodyString = "";
                        try { if (response.errorBody() != null) responseBodyString = response.errorBody().string(); } catch (IOException e) { /* ignore */ }
                        Log.e(TAG, errorMsg + (!responseBodyString.isEmpty() ? "\nError Body: "+responseBodyString : ""));
                        callback.onError("Upload failed: " + response.code());
                    }
                }
                @Override
                public void onFailure(@NonNull Call<ImageUploadResponse> call, @NonNull Throwable t) {
                    // --- SỬA ĐỔI: Kiểm tra Activity hợp lệ ---
                    if (isFinishing() || isDestroyed()) {
                        Log.w(TAG, "uploadImageToServer onFailure: Activity is finishing or destroyed. Ignoring callback.");
                        return;
                    }
                    // --- SỬA ĐỔI: Kiểm tra xem có phải lỗi do call bị cancel không ---
                    if (call.isCanceled()) {
                        Log.d(TAG, "uploadImageToServer onFailure: Call was cancelled.");
                    } else {
                        Log.e(TAG, "Upload network failure: " + t.getMessage(), t);
                        callback.onError("Network Error: " + t.getMessage());
                    }
                }
            });
        } catch (IOException e) {
            Log.e(TAG, "IOException processing image URI: " + e.getMessage(), e);
            callback.onError("Error reading image file");
        } catch (OutOfMemoryError e) {
            Log.e(TAG, "OutOfMemoryError reading image", e);
            callback.onError("Image file is too large");
        } finally {
            if (inputStream != null) {
                try { inputStream.close(); } catch (IOException e) { Log.e(TAG, "Error closing InputStream", e); }
            }
        }
    }

    // Hàm trợ giúp lấy tên file từ Uri (cải thiện)
    private String getFileNameFromUri(Uri uri) {
        String result = null;
        if (uri != null && ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
            try (Cursor cursor = getContentResolver().query(uri, new String[]{MediaStore.MediaColumns.DISPLAY_NAME}, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex);
                    }
                }
            } catch (Exception e) {
                Log.w(TAG, "Error getting filename from ContentResolver for URI: " + uri, e);
            }
        }
        if (result == null && uri != null) {
            result = uri.getLastPathSegment();
            // Một số URI không có last path segment rõ ràng (vd: từ Google Photos)
        }
        // Làm sạch tên file và tạo tên dự phòng nếu cần
        if (result != null) {
            result = result.replaceAll("[^a-zA-Z0-9.\\-_]", "_"); // Thay các ký tự không hợp lệ
        }
        // Tạo tên file dự phòng nếu không lấy được hoặc tên trống
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

        // Hủy call cũ nếu có
        cancelCall(createCommentCall);
        createCommentCall = ApiService.apiService.createComment(requestBody); // Gán call mới

        // Gọi API để tạo comment
        createCommentCall.enqueue(new Callback<Comment>() { // Đảm bảo Model Comment đúng
            @Override
            public void onResponse(@NonNull Call<Comment> call, @NonNull Response<Comment> response) {
                // --- SỬA ĐỔI: Kiểm tra Activity hợp lệ ---
                if (isFinishing() || isDestroyed()) {
                    Log.w(TAG, "sendCommentData onResponse: Activity is finishing or destroyed. Ignoring callback.");
                    return;
                }

                // Quan trọng: Chỉ ẩn loading nếu không có call nào khác đang chạy
                // Hoặc đơn giản hóa: luôn ẩn ở đây nếu logic loading đơn giản
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    // Tạo comment thành công
                    Log.i(TAG, "Comment created successfully! ID: " + response.body().getCommentId());
                    Toast.makeText(ProductRatingActivity.this, "Review submitted!", Toast.LENGTH_SHORT).show();
                    // Reset lại UI nhập liệu (kiểm tra null trước khi dùng)
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
                }
                // Cập nhật lại trạng thái nút gửi sau khi xử lý xong
                updateSendButtonState();
            }

            @Override
            public void onFailure(@NonNull Call<Comment> call, @NonNull Throwable t) {
                // --- SỬA ĐỔI: Kiểm tra Activity hợp lệ ---
                if (isFinishing() || isDestroyed()) {
                    Log.w(TAG, "sendCommentData onFailure: Activity is finishing or destroyed. Ignoring callback.");
                    return;
                }
                // --- SỬA ĐỔI: Kiểm tra xem có phải lỗi do call bị cancel không ---
                if (call.isCanceled()) {
                    Log.d(TAG, "sendCommentData onFailure: Call was cancelled.");
                } else {
                    Log.e(TAG, "Create comment NETWORK failure: " + t.getMessage(), t);
                    Toast.makeText(ProductRatingActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }

                showLoading(false); // Tắt loading khi có lỗi mạng
                updateSendButtonState(); // Cập nhật nút sau lỗi
            }
        });
    }

    // Tải danh sách comment từ server
    private void fetchComments() {
        Log.d(TAG, "Fetching comments for product ID: " + productId);
        showLoading(true); // Bật loading trước khi gọi API

        // Hủy call fetch cũ nếu có
        cancelCall(fetchCommentsCall);
        fetchCommentsCall = ApiService.apiService.getCommentsByProduct(productId); // Gán call mới

        fetchCommentsCall.enqueue(new Callback<List<Comment>>() { // Đảm bảo Model Comment đúng
            @Override
            public void onResponse(@NonNull Call<List<Comment>> call, @NonNull Response<List<Comment>> response) {
                // --- SỬA ĐỔI: Kiểm tra Activity hợp lệ ---
                if (isFinishing() || isDestroyed()) {
                    Log.w(TAG, "fetchComments onResponse: Activity is finishing or destroyed. Ignoring callback.");
                    return;
                }

                showLoading(false); // Tắt loading khi có response

                if (response.isSuccessful() && response.body() != null) {
                    List<Comment> comments = response.body();
                    Log.d(TAG, "Successfully fetched " + comments.size() + " comments.");
                    // --- SỬA ĐỔI: Thêm kiểm tra null cho các View trước khi dùng ---
                    if (comments.isEmpty()) {
                        if(tvNoComments != null) { tvNoComments.setText("Be the first to review!"); tvNoComments.setVisibility(View.VISIBLE); }
                        if (recyclerViewComments != null) recyclerViewComments.setVisibility(View.GONE);
                    } else {
                        if(tvNoComments != null) tvNoComments.setVisibility(View.GONE);
                        if (recyclerViewComments != null) recyclerViewComments.setVisibility(View.VISIBLE);
                        if (commentAdapter != null) commentAdapter.setComments(comments); // Cập nhật adapter
                    }
                } else {
                    // Lỗi khi tải comment
                    Log.e(TAG, "Failed to load comments: " + response.code() + " - " + response.message());
                    // --- SỬA ĐỔI: Thêm kiểm tra null cho các View trước khi dùng ---
                    if(tvNoComments != null) { tvNoComments.setText("Failed to load reviews."); tvNoComments.setVisibility(View.VISIBLE); }
                    if (recyclerViewComments != null) recyclerViewComments.setVisibility(View.GONE);
                    Toast.makeText(ProductRatingActivity.this, "Failed load: " + response.code(), Toast.LENGTH_SHORT).show();
                }
                updateSendButtonState(); // Cập nhật nút sau khi fetch xong
            }

            @Override
            public void onFailure(@NonNull Call<List<Comment>> call, @NonNull Throwable t) {
                // --- SỬA ĐỔI: Kiểm tra Activity hợp lệ ---
                if (isFinishing() || isDestroyed()) {
                    Log.w(TAG, "fetchComments onFailure: Activity is finishing or destroyed. Ignoring callback.");
                    return;
                }
                // --- SỬA ĐỔI: Kiểm tra xem có phải lỗi do call bị cancel không ---
                if (call.isCanceled()) {
                    Log.d(TAG, "fetchComments onFailure: Call was cancelled.");
                } else {
                    Log.e(TAG, "Error fetching comments (Network): " + t.getMessage(), t);
                    // --- SỬA ĐỔI: Thêm kiểm tra null cho các View trước khi dùng ---
                    if(tvNoComments != null) { tvNoComments.setText("Network error loading reviews."); tvNoComments.setVisibility(View.VISIBLE); }
                    if (recyclerViewComments != null) recyclerViewComments.setVisibility(View.GONE);
                    Toast.makeText(ProductRatingActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
                showLoading(false); // Tắt loading khi có lỗi mạng
                updateSendButtonState(); // Cập nhật nút sau lỗi
            }
        });
    }

    // Hiển thị/ẩn ProgressBar và bật/tắt các input liên quan
    private void showLoading(boolean isLoading) {
        Log.d(TAG, "Setting loading state to: " + isLoading);
        if (progressBar != null) {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        } else {
            Log.w(TAG, "showLoading: progressBar is null!"); // Log cảnh báo nếu progressBar bị null
        }

        // Chỉ enable/disable input nếu chúng đang hiển thị và không null
        boolean isInputSectionVisible = (ratingBar != null && ratingBar.getVisibility() == View.VISIBLE);

        if (isInputSectionVisible) {
            if (ivAttachComment != null) ivAttachComment.setEnabled(!isLoading);
            if (etCommentInput != null) etCommentInput.setEnabled(!isLoading);
            if (ratingBar != null) ratingBar.setEnabled(!isLoading);
            if (ivClearSelectedImage != null) ivClearSelectedImage.setEnabled(!isLoading); // Cũng tắt nút xóa ảnh khi loading
        }
        // Luôn gọi updateSendButtonState để nó tự quyết định trạng thái nút gửi
        updateSendButtonState();
    }


    // --- Xử lý nút Back trên Toolbar (Navigation Icon) ---
    @Override
    public boolean onSupportNavigateUp() {
        // Kiểm tra nếu đang loading thì không cho back
        if (progressBar != null && progressBar.getVisibility() == View.VISIBLE) {
            Toast.makeText(this, "Please wait...", Toast.LENGTH_SHORT).show();
            return true; // Đã xử lý (bằng cách chặn lại)
        }
        // Sử dụng hành vi mặc định (finish activity hiện tại)
        onBackPressed(); // Gọi hành động back mặc định
        return true; // Báo rằng chúng ta đã xử lý sự kiện này
    }

    // --- Xử lý nút Back vật lý ---
    @Override
    public void onBackPressed() {
        // Kiểm tra nếu đang loading thì không cho back
        if (progressBar != null && progressBar.getVisibility() == View.VISIBLE) {
            Toast.makeText(this, "Please wait...", Toast.LENGTH_SHORT).show();
            // Không gọi super.onBackPressed() để chặn hành động back
        } else {
            // Nếu không loading, thực hiện hành động back mặc định (đóng Activity)
            super.onBackPressed();
        }
    }

    // --- Xử lý các Item khác trên Menu (nếu có) ---
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Không cần xử lý android.R.id.home ở đây vì onSupportNavigateUp đã xử lý nó
        return super.onOptionsItemSelected(item);
    }

    // --- Hủy các cuộc gọi mạng khi Activity bị hủy ---
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: Cancelling ongoing network calls.");
        cancelCall(imageUploadCall);
        cancelCall(createCommentCall);
        cancelCall(fetchCommentsCall);
    }

    // Hàm tiện ích để hủy Retrofit Call nếu nó không null và chưa bị hủy
    private void cancelCall(Call<?> call) {
        if (call != null && !call.isCanceled() && !call.isExecuted()) {
            call.cancel();
        }
    }
}