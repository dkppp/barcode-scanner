# Example
```Gradle
dependencies {
    compile 'com.github.dkppp:barcode-scanner:1.0.1'
}
```

[![Release](https://jitpack.io/v/User/Repo.svg)](https://jitpack.io/dkppp/barcode-scanner)

activity_card_scan.xml
```XML
<?xml version="1.0" encoding="utf-8"?>
<ru.vigroup.barcodescanner.ZXingScannerView
    android:id="@+id/scanner"
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    app:borderColor="@color/colorPrimary">
    
    <!--put here childs, ZXingScannerView extends FrameLayout-->
    
</ru.vigroup.barcodescanner.ZXingScannerView>    
```


```XML
<attr name="maskColor" format="color|reference"/>
<attr name="borderLength" format="dimension|reference"/>
<attr name="borderStrokeWidth" format="dimension|reference"/>
<attr name="borderPadding" format="dimension|reference"/>
<attr name="borderColor" format="color|reference"/>
```

```Java
public class CardScanActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {
    public static final String EXTRA_CARD = "card";

    @Bind(R.id.scanner)
    protected ZXingScannerView mScannerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_scan);
        ButterKnife.bind(this);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this);
        mScannerView.setAutoFocus(true);
        mScannerView.startCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void handleResult(Result result) {
        Intent data = new Intent();
        data.putExtra(EXTRA_CARD, result.getText());

        setResult(Activity.RESULT_OK, data);
        finish();
    }
}
```
