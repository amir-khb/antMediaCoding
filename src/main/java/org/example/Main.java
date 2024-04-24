package org.example;

import org.bytedeco.ffmpeg.avcodec.AVCodec;
import org.bytedeco.ffmpeg.avcodec.AVCodecParameters;
import org.bytedeco.ffmpeg.avcodec.AVPacket;
import org.bytedeco.ffmpeg.avformat.AVFormatContext;
import org.bytedeco.ffmpeg.avformat.AVInputFormat;
import org.bytedeco.ffmpeg.avformat.AVIOContext;
import org.bytedeco.ffmpeg.avformat.AVStream;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avformat;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.PointerPointer;

import static org.bytedeco.ffmpeg.global.avcodec.*;
import static org.bytedeco.ffmpeg.global.avformat.*;
import static org.bytedeco.ffmpeg.global.avutil.*;

public class Main {
    public static void main(String[] args) {
        String inputPath = "/Users/amir/IdeaProjects/antMediaCoding/src/main/java/org/example/input.flv";
        String outputPath = "/Users/amir/IdeaProjects/antMediaCoding/src/main/java/org/example/output.mp4";


        // Allocate AVFormatContext and open input file
        AVFormatContext inputFormatContext = new AVFormatContext(null);
        if (avformat_open_input(inputFormatContext, inputPath, null, null) != 0) {
            System.err.println("Error: Couldn't open file.");
            return;
        }

        // Retrieve stream information
        if (avformat_find_stream_info(inputFormatContext, (PointerPointer) null) < 0) {
            System.err.println("Error: Couldn't find stream information.");
            return;
        }

        // Open output file
        AVFormatContext outputFormatContext = new AVFormatContext(null);
        if (avformat_alloc_output_context2(outputFormatContext, null, null, outputPath) < 0) {
            System.err.println("Error: Couldn't create output context.");
            return;
        }

        // Find encoder for MP4 format
        AVCodec mp4Codec = avcodec_find_encoder(AV_CODEC_ID_MPEG4);;
        if (mp4Codec == null) {
            System.err.println("Error: Codec not found.");
            return;
        }
        System.out.println("Codec: " + mp4Codec.long_name().getString());


        for (int i = 0; i < inputFormatContext.nb_streams(); i++) {
            AVStream stream = inputFormatContext.streams(i);
            AVCodecParameters codecParams = stream.codecpar();
            AVCodec codec = avcodec.avcodec_find_decoder(codecParams.codec_id());

            System.out.println("Stream #" + i);
            System.out.println("  Codec Name: " + codec.long_name().getString());
            System.out.println("  Codec Type: " + codecParams.codec_type());
            // Add more information as needed
        }


        // Read and process the packets (replace this with your desired logic)
        AVPacket packet = new AVPacket();
        while (av_read_frame(inputFormatContext, packet) >= 0) {
            // Process the packet data
            // ...

            av_packet_unref(packet);
        }

        avformat_close_input(inputFormatContext);


    }
}