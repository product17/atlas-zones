package io.sandbox.zones.processors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.sandbox.zones.Main;
import net.minecraft.structure.processor.StructureProcessorType;
import net.minecraft.util.Identifier;

public class ProcessorLoader {
  private static final Map<Identifier, List<? extends ZoneProcessorBase>> PROCESSOR_REGISTRY = new HashMap<>();
  public static StructureProcessorType<JigsawProcessor> JIGSAW_PROCESSOR = () -> JigsawProcessor.CODEC;

  public static ZoneProcessorBase getProcessor(Identifier identifier) {
    List<? extends ZoneProcessorBase> list = ProcessorLoader.PROCESSOR_REGISTRY.get(identifier);
    return list.size() > 0 ? list.get(0) : null;
  }

  public static void init() {
    ProcessorLoader.registerProcessor(
      Main.id(JigsawProcessor.NAME),
      new ArrayList<>(Arrays.asList(new JigsawProcessor()))
    );
    ProcessorLoader.registerProcessor(
      Main.id(SpawnProcessor.NAME),
      new ArrayList<>(Arrays.asList(new SpawnProcessor()))
    );
    ProcessorLoader.registerProcessor(
      Main.id(CleanupProcessor.NAME),
      new ArrayList<>(Arrays.asList(new CleanupProcessor()))
    );
  }

  public static void registerProcessor(Identifier identifier, List<? extends ZoneProcessorBase> processor) {
    ProcessorLoader.PROCESSOR_REGISTRY.put(identifier, processor);
  }
}
