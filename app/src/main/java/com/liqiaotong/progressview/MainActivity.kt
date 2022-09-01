package com.liqiaotong.progressview

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.liqiaotong.lib.OnAnProgressViewListener
import com.liqiaotong.progressview.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.anProgress.setOnAnProgressViewListener(object:OnAnProgressViewListener{
            override fun onSelected(progress: Int, index: Int) {
                binding.tvProgress2.text = "progress:$progress  index:$index"
            }

            override fun onProgress(progressF: Float, progress: Int) {
                binding.tvProgress1.text = "progressF:$progressF  progress:$progress"
            }
        })

        binding.b1.setOnClickListener {
            binding.anProgress.setProgress(0f,true)
        }

        binding.b2.setOnClickListener {
            binding.anProgress.setProgress(0.25f,true)
        }

        binding.b3.setOnClickListener {
            binding.anProgress.setProgress(0.5f,true)
        }

        binding.b4.setOnClickListener {
            binding.anProgress.setProgress(0.75f,true)
        }

        binding.b5.setOnClickListener {
            binding.anProgress.setProgress(1f,true)
        }

    }
}