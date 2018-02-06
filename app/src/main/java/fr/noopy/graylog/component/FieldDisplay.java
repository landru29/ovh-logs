package fr.noopy.graylog.component;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;

import fr.noopy.graylog.R;

/**
 * Created by cyrille on 06/02/18.
 */

    /**
     * Created by cyrille on 08/12/17.
     */

    public class FieldDisplay extends RelativeLayout {

        private TextView label;
        private TextView value;

        public FieldDisplay(final Context context, final AttributeSet attrs, final int defStyle) {
            super(context, attrs, defStyle);

            inflate(getContext(), R.layout.component_field_display, this);
            label = (TextView)findViewById(R.id.label);
            value = (TextView)findViewById(R.id.value);

            TypedArray a = context.getTheme().obtainStyledAttributes(
                    attrs,
                    R.styleable.DisplayFeature,
                    0, 0);

            try {
                setLabel(a.getString(R.styleable.DisplayFeature_label));
                setValue(a.getString(R.styleable.DisplayFeature_value));
            } finally {
                a.recycle();
            }

        }

        public FieldDisplay(Context context, AttributeSet attrs) {
            this(context, attrs, 0);
        }

        public FieldDisplay(Context context) {
            this(context, null, 0);
        }

        public void setLabel (String label) {
            this.label.setText(label);
        }

        public void setValue (String value) {
            this.value.setText(value);
        }

    }
