package org.example;

import org.bytedeco.ffmpeg.avcodec.AVCodecParameters;
import org.bytedeco.ffmpeg.avformat.AVFormatContext;
import org.bytedeco.ffmpeg.avformat.AVStream;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avformat;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.ffmpeg.avcodec.AVCodecContext;
import org.bytedeco.ffmpeg.avcodec.AVPacket;
import org.bytedeco.ffmpeg.avutil.AVDictionary;
import org.bytedeco.javacpp.PointerPointer;

import static org.bytedeco.javacpp.avformat.av_register_all;

public class Main {

    static {
        // Initialize FFmpeg
        av_register_all();
    }

    public static void convertFlvToMp4(String inputFlv, String outputMp4) throws Exception {
        AVFormatContext inputFormatContext = new AVFormatContext(null);
        if (avformat.avformat_open_input(inputFormatContext, inputFlv, null, null) < 0) {
            throw new Exception("Could not open input FLV file.");
        }

        if (avformat.avformat_find_stream_info(inputFormatContext, (PointerPointer) null) < 0) {
            throw new Exception("Could not find stream information.");
        }

        // Allocate output context
        AVFormatContext outputFormatContext = new AVFormatContext(null);
        avformat.avformat_alloc_output_context2(outputFormatContext, null, "mp4", outputMp4);

        for (int i = 0; i < inputFormatContext.nb_streams(); i++) {
            AVStream inputStream = inputFormatContext.streams(i);
            AVCodecParameters codecParameters = inputStream.codecpar();

            AVStream outputStream = avformat.avformat_new_stream(outputFormatContext, null);

            AVCodecContext codecContext = avcodec.avcodec_alloc_context3(null);
            avcodec.avcodec_parameters_to_context(codecContext, codecParameters);

            avcodec.avcodec_parameters_copy(outputStream.codecpar(), codecParameters);

            outputStream.time_base(inputStream.time_base());
        }

        if (avformat.avio_open(outputFormatContext.pb(), outputMp4, avformat.AVIO_FLAG_WRITE) < 0) {
            throw new Exception("Could not open output MP4 file for writing.");
        }

        // Write header
        if (avformat.avformat_write_header(outputFormatContext, (PointerPointer) null) < 0) {
            throw new Exception("Could not write output header.");
        }

        AVPacket packet = new AVPacket();
        while (avformat.av_read_frame(inputFormatContext, packet) >= 0) {
            AVStream outputStream = outputFormatContext.streams(packet.stream_index());

            packet.pts(avutil.av_rescale_q(packet.pts(), inputFormatContext.streams(packet.stream_index()).time_base(), outputStream.time_base()));
            packet.dts(avutil.av_rescale_q(packet.dts(), inputFormatContext.streams(packet.stream_index()).time_base(), outputStream.time_base()));

            packet.pos(-1);

            if (avformat.av_interleaved_write_frame(outputFormatContext, packet) < 0) {
                throw new Exception("Error writing frame to output.");
            }

            avcodec.av_packet_unref(packet);
        }

        if (avformat.av_write_trailer(outputFormatContext) < 0) {
            throw new Exception("Error writing output trailer.");
        }

        avformat.avformat_close_input(inputFormatContext);
        avformat.avio_close(outputFormatContext.pb());
        avutil.av_free(outputFormatContext);
    }

    public static void main(String[] args) {
//        if (args.length != 2) {
//            System.out.println("Usage: FlvToMp4Converter <input.flv> <output.mp4>");
//            return;
//        }

        try {
            convertFlvToMp4("/Users/amir/IdeaProjects/antMediaCoding/src/main/java/org/example/input.flv",
                    "/Users/amir/IdeaProjects/antMediaCoding/src/main/java/org/example/input.mp4");
            System.out.println("Conversion completed successfully.");
        } catch (Exception e) {
            System.err.println("Error during conversion: " + e.getMessage());
        }
    }
}